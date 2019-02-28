package darkyenuscommand.match;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

/**
 *
 */
public final class Match<T> {

	private final boolean success;
	private final T result;
	private String message;
	private final T[] suggestions;

	private String defaultNoun;

	private Match(boolean success, @Nullable T result, @Nullable String message, @Nullable T[] suggestions) {
		this.success = success;
		this.result = result;
		this.message = message;
		this.suggestions = suggestions;
	}

	public boolean success() {
		return success;
	}

	@Nullable
	public T successResult() {
		assert success();
		return result;
	}

	public T[] suggestions() {
		assert !success();
		return suggestions;
	}

	@SuppressWarnings("unchecked")
	public T[] asArray(Class<T> type) {
		if (result != null) {
			final T[] resultA = (T[]) Array.newInstance(type, 1);
			resultA[0] = result;
			return resultA;
		} else if (suggestions != null){
			return suggestions;
		} else {
			return (T[]) Array.newInstance(type, 0);
		}
	}

	public Match<T> withDefaultNoun(String noun) {
		this.defaultNoun = noun;
		return this;
	}

	public String suggestionMessage() {
		assert !success();
		if (message == null) {
			final T[] suggestions = this.suggestions;
			String noun = defaultNoun;
			if (noun == null) {
				noun = "Item";
			}

			if(suggestions == null || suggestions.length == 0){
				message = ChatColor.RED+noun+" not found";
			} else if(suggestions.length == 1){
				message = (ChatColor.RED+noun+" not found "+ChatColor.DARK_RED+"Did you mean: " + ChatColor.RESET + suggestions[0] + ChatColor.DARK_RED + " ?");
			} else {
				final StringBuilder sb = new StringBuilder(ChatColor.RED+noun+" not found "+ChatColor.DARK_RED+"Did you mean: " + ChatColor.RESET);
				for (int i = 0; i < suggestions.length; i++) {
					if(i != 0) {
						sb.append(ChatColor.DARK_RED);
						sb.append(i + 1 == suggestions.length ? " or " : ", ");
						sb.append(ChatColor.RESET);
					}
					sb.append(suggestions[i]);
				}
				sb.append(ChatColor.DARK_RED).append('?');
				message = sb.toString();
			}
		}

		return message;
	}

	@NotNull
	public static <T> Match<T> success(@Nullable T item) {
		return new Match<>(true, item, null, null);
	}

	@NotNull
	public static <T> Match<T> failure(@Nullable String noun, @NotNull T[] suggestions) {
		return new Match<>(false, null, null, suggestions).withDefaultNoun(noun);
	}

	@NotNull
	public static <T> Match<T> failure(@NotNull String message) {
		return new Match<>(false, null, message, null);
	}
}
