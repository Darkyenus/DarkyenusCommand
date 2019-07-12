package darkyenuscommand.command;

import darkyenuscommand.Plugin;
import darkyenuscommand.command.argument.*;
import darkyenuscommand.match.Match;
import darkyenuscommand.match.MatchUtils;
import darkyenuscommand.util.Parameters;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static darkyenuscommand.command.argument.EnumArgument.prettyEnumName;

/**
 *
 */
public final class CommandProcessor implements CommandExecutor, TabCompleter {

	private static final Logger LOG = Plugin.logger(CommandProcessor.class);

	private final CommandMethod[] methods;

	private CommandProcessor(Object instance, ArrayList<Method> methods) {
		final CommandMethod[] commandMethods = new CommandMethod[methods.size()];
		for (int i = 0; i < methods.size(); i++) {
			final Method method = methods.get(i);
			final Parameter[] parameters = method.getParameters();
			if (parameters.length <= 0) {
				throw new IllegalArgumentException("Command method "+method+" has no sender parameter");
			}
			final Parameter senderParameter = parameters[0];
			final boolean needsPlayerSender;
			if (CommandSender.class.equals(senderParameter.getType())) {
				needsPlayerSender = false;
			} else if (Player.class.equals(senderParameter.getType())) {
				needsPlayerSender = true;
			} else {
				throw new IllegalArgumentException("Command method "+method+" has no sender parameter, only CommandSender and Player types are allowed");
			}

			final int offset = 1;
			final Argument[] matchers = new Argument[parameters.length - offset];
			for (int j = offset; j < parameters.length; j++) {
				matchers[j - offset] = createMatcher(parameters[j]);
			}
			commandMethods[i] = new CommandMethod(needsPlayerSender, instance, method, matchers);
		}
		this.methods = commandMethods;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		StringBuilder failures = new StringBuilder();

		for (CommandMethod method : methods) {
			final CommandMethod.ArgumentBinding binding = method.createArgumentBinding(sender, args);
			if (binding.error == null) {
				// Successful binding, use it
				assert binding.matched == binding.arguments.length;
				try {
					method.method.invoke(method.instance, binding.arguments);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Could not access " + method.method, e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Failure while evaluating " + method.method, e.getCause());
				}
				return true;
			}

			if (failures.length() > 0) {
				failures.append('\n');
			}
			failures.append(ChatColor.WHITE).append(method.usageArguments).append(' ');
			failures.append(ChatColor.RED).append(binding.error);
		}

		if (failures.length() > 0) {
			sender.sendMessage(failures.toString());
			return true;
		}
		return false;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		final ArrayList<StringWithScore> completions = new ArrayList<>();
		for (CommandMethod method : methods) {
			method.tabCompleteArgument(sender, args, completions::add);
		}

		final ArrayList<String> sortedCompletions = new ArrayList<>(completions.size());
		// Sadly, this sorting is useless, because brigadier will sort it again
		completions.sort(StringWithScore::compareTo);
		completions.forEach((withScore) -> sortedCompletions.add(withScore.value));
		return sortedCompletions;
	}

	private String createUsageString(String name) {
		final StringBuilder sb = new StringBuilder();
		for (CommandMethod method : methods) {
			if (sb.length() != 0) {
				sb.append('\n');
			}
			sb.append('/').append(name);
			sb.append(' ').append(method.usageArguments);

			final Cmd cmd = method.method.getDeclaredAnnotation(Cmd.class);
			if (cmd != null) {
				final String description = cmd.description();
				if (!description.isEmpty()) {
					sb.append(' ').append(ChatColor.ITALIC).append(description).append(ChatColor.RESET);
				}
			}
		}

		return sb.toString();
	}

	public static void registerCommandsAndEvents(JavaPlugin plugin, Object commandHolder) {
		if (commandHolder instanceof Listener) {
			plugin.getServer().getPluginManager().registerEvents((Listener) commandHolder, plugin);
		}
		registerCommands(plugin, commandHolder.getClass(), commandHolder);
	}

	public static void registerCommands(JavaPlugin plugin, Class<?> commandHolderClass, Object commandHolder) {
		final HashMap<PluginCommand, ArrayList<Method>> foundCommands = new HashMap<>();

		for (Method method : commandHolderClass.getDeclaredMethods()) {
			final Cmd cmd = method.getDeclaredAnnotation(Cmd.class);
			if (cmd == null) {
				continue;
			}
			if (method.isAccessible()) {
				LOG.log(Level.WARNING, "Method "+method+" can't be considered a command - not accessible");
				continue;
			}

			String commandName = cmd.value();
			if (commandName.isEmpty()) {
				commandName = method.getName();
			}

			final PluginCommand command = plugin.getCommand(commandName);
			if (command == null) {
				LOG.log(Level.WARNING, "Method "+method+" can't be considered a command - command \""+commandName+"\" not registered");
				continue;
			}

			ArrayList<Method> commandMethods = foundCommands.computeIfAbsent(command, k -> new ArrayList<>());
			commandMethods.add(method);
		}

		foundCommands.forEach((command, methods) -> {
			if (methods.size() > 1) {
				methods.sort((first, second) -> {
					final Cmd firstCmd = first.getDeclaredAnnotation(Cmd.class);
					final Cmd secondCmd = second.getDeclaredAnnotation(Cmd.class);
					return Integer.compare(firstCmd.order(), secondCmd.order());
				});
			}
			final CommandProcessor processor = new CommandProcessor(commandHolder, methods);
			command.setUsage(processor.createUsageString(command.getName()));
			command.setExecutor(processor);
			command.setTabCompleter(processor);
		});
	}

	private static Argument createMatcher(String symbol, Class<?> type) {
		if (String.class.equals(type)) {
			return new StringArgument(symbol);
		} else if (Integer.TYPE.equals(type)) {
			return new IntArgument(symbol);
		} else if (Float.TYPE.equals(type)) {
			return new FloatArgument(symbol);
		} else if (Double.TYPE.equals(type)) {
			return new DoubleArgument(symbol);
		} else if (Player.class.equals(type)) {
			return new PlayerArgument(symbol);
		} else if (OfflinePlayer.class.equals(type)) {
			return new OfflinePlayerArgument(symbol);
		} else if (World.class.equals(type)) {
			return new WorldArgument(symbol);
		} else if (Material.class.equals(type)) {
			return new MaterialArgument(symbol);
		}

		throw new IllegalArgumentException("Argument matcher for "+type+" is not implemented");
	}

	private static <T> Argument<T> wrapMatcherWithPrefix(Argument<T> matcher, String prefix) {
		return new Argument<T>(matcher.symbol, matcher.type) {
			@NotNull
			@Override
			public Match<T> match(@NotNull CommandSender sender, @NotNull Parameters params) {
				final String peek = params.peek();
				if (peek == null || !peek.startsWith(prefix)) {
					return Match.failure("Prefix '"+prefix+"' expected");
				}
				Parameters newParams = params.copy();
				newParams.args[0] = peek.substring(prefix.length());
				final Match<T> match = matcher.match(sender, newParams);
				if (match.success()) {
					params.index += newParams.index;
				}
				return match;
			}

			@Override
			public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
				matcher.suggest(sender, (suggestion) -> suggestionConsumer.accept(prefix + suggestion));
			}
		};
	}

	private static <T> Argument<T> wrapMatcherWithVarArg(Argument<T> matcher, String separator) {
		return new Argument<T>(matcher.symbol, matcher.type) {
			@NotNull
			@Override
			public Match<T> match(@NotNull CommandSender sender, @NotNull Parameters params) {
				if (params.eof()) {
					return missing();
				}

				final String collected = params.rest(separator);
				final Parameters newParams = new Parameters(new String[]{ collected });
				final Match<T> match = matcher.match(sender, newParams);
				params.index = params.end;
				return match;
			}

			@Override
			public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
				matcher.suggest(sender, suggestionConsumer);
			}
		};
	}

	private static <T> Argument<T> wrapMatcherWithDefault(Argument<T> matcher) {
		return new Argument<T>(matcher.symbol, matcher.type) {
			@SuppressWarnings("unchecked")
			@NotNull
			@Override
			public Match<T> match(@NotNull CommandSender sender, @NotNull Parameters params) {
				final int mark = params.mark();
				final Match<T> match = matcher.match(sender, params);
				if (match.success()) {
					return match;
				} else {
					params.rollback(mark);
					Object otherwise = null;
					if (matcher.type == Byte.TYPE) {
						otherwise = (byte) 0;
					} else if (matcher.type == Character.TYPE) {
						otherwise = '\0';
					} else if (matcher.type == Short.TYPE) {
						otherwise = (short) 0;
					} else if (matcher.type == Integer.TYPE) {
						otherwise = 0;
					} else if (matcher.type == Long.TYPE) {
						otherwise = 0L;
					} else if (matcher.type == Float.TYPE) {
						otherwise = 0.0f;
					} else if (matcher.type == Double.TYPE) {
						otherwise = 0.0;
					} else if (matcher.type == Boolean.TYPE) {
						otherwise = false;
					}

					return Match.success((T)otherwise);
				}
			}

			@Override
			public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
				matcher.suggest(sender, suggestionConsumer);
			}
		};
	}

	private static final Map<Class<?>, Function<CommandSender, ?>> IMPLICIT_GENERATORS = new HashMap<>();
	static {
		IMPLICIT_GENERATORS.put(Player.class, (sender) -> {
			if (sender instanceof Player) {
				return sender;
			}
			return null;
		});
		IMPLICIT_GENERATORS.put(World.class, (sender) -> {
			if (sender instanceof Player) {
				return ((Player) sender).getWorld();
			}
			return null;
		});
	}

	private static <T> Argument<T> wrapMatcherWithImplicit(Argument<T> matcher) {
		final Function<CommandSender, ?> implicitGenerator = IMPLICIT_GENERATORS.get(matcher.type);
		if (implicitGenerator == null) {
			throw new IllegalArgumentException("No implicit generator available for "+matcher.type);
		}

		return new Argument<T>(matcher.symbol, matcher.type) {
			@SuppressWarnings("unchecked")
			@NotNull
			@Override
			public Match<T> match(@NotNull CommandSender sender, @NotNull Parameters params) {
				final int mark = params.mark();
				final Match<T> match = matcher.match(sender, params);
				if (match.success()) {
					return match;
				}
				params.rollback(mark);
				final Object implicit = implicitGenerator.apply(sender);
				if (implicit == null) {
					return match;
				}

				return Match.success((T)implicit);
			}

			@Override
			public void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer) {
				matcher.suggest(sender, suggestionConsumer);
			}
		};
	}

	@SuppressWarnings("unchecked")
	@NotNull
	private static Argument createMatcher(@NotNull Parameter param) {
		try {
			final boolean useImplicit = param.getDeclaredAnnotation(Cmd.UseImplicit.class) != null;
			final boolean useDefault = param.getDeclaredAnnotation(Cmd.UseDefault.class) != null;
			final Cmd.Prefix prefix = param.getDeclaredAnnotation(Cmd.Prefix.class);
			final Cmd.VarArg varArg = param.getDeclaredAnnotation(Cmd.VarArg.class);
			final Enum[] enumConstants = Material.class.equals(param.getType()) ? null : Cmd.Util.viableEnumConstants(param);

			final String symbol;
			{
				final StringBuilder sb = new StringBuilder();

				if (prefix != null) {
					sb.append(prefix.value());
				}
				final boolean optional = useDefault || useImplicit;
				sb.append(optional ? '[' : '<');

				if (enumConstants != null) {
					for (int i = 0; i < enumConstants.length; i++) {
						if (i > 0) {
							sb.append(" | ");
						}
						sb.append('"').append(prettyEnumName(enumConstants[i])).append('"');
					}
				} else {
					sb.append(param.getName());
				}

				sb.append(optional ? ']' : '>');
				symbol = sb.toString();
			}

			final Class<?> type = param.getType();
			Argument matcher = enumConstants == null ? createMatcher(symbol, type) : new EnumArgument<>(symbol, (Class<Enum>) type, enumConstants);

			if (varArg != null) {
				matcher = wrapMatcherWithVarArg(matcher, varArg.separator());
			}

			if (prefix != null && !prefix.value().isEmpty()) {
				matcher = wrapMatcherWithPrefix(matcher, prefix.value());
			}

			if (useImplicit) {
				matcher = wrapMatcherWithImplicit(matcher);
			}
			if (useDefault) {
				matcher = wrapMatcherWithDefault(matcher);
			}
			return matcher;
		} catch (Exception e) {
			throw new RuntimeException("Failed to create matcher for "+param, e);
		}
	}

	public static abstract class Argument<T> {

		public final String symbol;
		public final Class<T> type;

		protected Argument(@NotNull String symbol, @NotNull Class<T> type) {
			this.symbol = symbol;
			this.type = type;
		}

		/**
		 * Attempt to match, starting at given {@code index} in {@code args}.
		 *
		 * @param sender which made the command
		 * @param params available params
		 * @return whether match was made, whether it was certain or uncertain, or null if no match was possible at all
		 */
		@NotNull
		public abstract Match<T> match(@NotNull CommandSender sender, @NotNull Parameters params);

		public abstract void suggest(@NotNull CommandSender sender, @NotNull Consumer<String> suggestionConsumer);

		protected Match<T> missing() {
			return Match.failure("Missing parameter " + symbol);
		}
	}

	private static final class CommandMethod {

		private final boolean needsPlayerSender;
		private final Method method;
		private final Object instance;
		private final Argument[] arguments;

		public final String usageArguments;

		private CommandMethod(boolean needsPlayerSender, Object instance, @NotNull Method method, @NotNull Argument[] arguments) {
			this.needsPlayerSender = needsPlayerSender;
			this.instance = Modifier.isStatic(method.getModifiers()) ? null : instance;
			this.method = method;
			this.arguments = arguments;

			final StringBuilder usage = new StringBuilder();
			for (Argument matcher : arguments) {
				if (usage.length() > 0) {
					usage.append(' ');
				}
				usage.append(matcher.symbol);
			}

			this.usageArguments = usage.toString();
		}

		public static final class ArgumentBinding {
			final Object[] arguments;
			final int matched;
			final String error;

			public ArgumentBinding(Object[] arguments, int matched, String error) {
				this.arguments = arguments;
				this.matched = matched;
				this.error = error;
			}
		}

		private ArgumentBinding createArgumentBinding(CommandSender sender, String[] args) {
			int methodArgIndex = 1;
			final Object[] methodArgs = new Object[methodArgIndex + arguments.length];
			methodArgs[0] = sender;
			if (needsPlayerSender && !(sender instanceof Player)) {
				return new ArgumentBinding(methodArgs, 0, "In-game only");
			}

			final Parameters params = new Parameters(args, 0, args.length);
			for (Argument argument : arguments) {
				final Match match = argument.match(sender, params);
				if (match.success()) {
					methodArgs[methodArgIndex++] = match.successResult();
				} else {
					return new ArgumentBinding(methodArgs, methodArgIndex, match.suggestionMessage());
				}
			}

			if (!params.eof()) {
				return new ArgumentBinding(methodArgs, methodArgIndex, "Unexpected extra parameters: "+params.rest(" "));
			}

			return new ArgumentBinding(methodArgs, methodArgIndex, null);
		}

		private void tabCompleteArgument(CommandSender sender, String[] args, Consumer<StringWithScore> suggestionConsumer) {
			final Parameters params = new Parameters(args, 0, args.length);
			for (Argument<?> argument : arguments) {
				if (params.remaining() <= 1) {
					final String completing = params.rest(" ").toLowerCase();
					argument.suggest(sender, (suggestion) -> {
						final String sanitizedSuggestion = suggestion.toLowerCase();
						if (sanitizedSuggestion.startsWith(completing)) {
							suggestionConsumer.accept(new StringWithScore(suggestion, -1));
						} else if (sanitizedSuggestion.contains(completing)) {
							suggestionConsumer.accept(new StringWithScore(suggestion, 0));
						} else {
							final int score = MatchUtils.levenshteinDistance(completing, sanitizedSuggestion, 1, 5, 5);
							if (score <= 9) {
								suggestionConsumer.accept(new StringWithScore(suggestion, score));
							}
						}
					});
				}

				final Match match = argument.match(sender, params);
				if (!match.success()) {
					break;
				}
			}
		}
	}

	private static final class StringWithScore implements Comparable<StringWithScore> {
		public final String value;
		/** Less is better. */
		public final int score;

		private StringWithScore(String value, int score) {
			this.value = value;
			this.score = score;
		}

		@Override
		public int compareTo(@NotNull CommandProcessor.StringWithScore o) {
			return Integer.compare(score, o.score);
		}
	}
}
