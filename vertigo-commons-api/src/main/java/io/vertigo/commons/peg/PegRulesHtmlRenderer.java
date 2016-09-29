package io.vertigo.commons.peg;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertigo.lang.Assertion;

public final class PegRulesHtmlRenderer {

	private int depth = 1;
	private final Map<PegRule<?>, String> ALL_RULES = new LinkedHashMap<>();
	private final Map<Integer, Map<String, PegGrammarRule<?>>> NAMED_RULES = new LinkedHashMap<>();

	private void grammar(final PegGrammarRule<?> grammarRule) {
		NAMED_RULES.get(depth).put(grammarRule.getRuleName(), grammarRule);
		populateGramar(grammarRule, "NonTerminal('" + grammarRule.getRuleName() + "', '#" + grammarRule.getRuleName() + "')");
	}

	private void optional(final PegOptionalRule<?> rule) {
		populateGramar(rule, "Optional(" + readGramar(rule.getRule()) + ")");
	}

	private void term(final PegTermRule term) {
		populateGramar(term, term.getExpression());
	}

	private void sequence(final PegSequenceRule rule) {
		populateGramar(rule, rule.getRules().isEmpty() ? "Skip()" : "Sequence("
				+ rule.getRules().stream()
						.map(subRule -> readGramar(subRule))
						.collect(Collectors.joining(", "))
				+ ")");
	}

	private void choice(final PegChoiceRule rule) {
		populateGramar(rule, rule.getRules().isEmpty() ? "Skip()" : "Choice(0,"
				+ rule.getRules().stream()
						.map(subRule -> readGramar(subRule))
						.collect(Collectors.joining(", "))
				+ ")");
	}

	private void many(final PegManyRule<?> rule) {
		populateGramar(rule, (rule.isEmptyAccepted() ? "Zero" : "One") + "OrMore(" + readGramar(rule.getRule()) + ")");
	}

	private void whiteSpace(final PegWhiteSpaceRule rule) {
		populateGramar(rule, "' '");
	}

	private void word(final PegWordRule rule) {
		populateGramar(rule, "NonTerminal('" + rule.getExpression() + "')");
	}

	private void populateGramar(final PegRule<?> rule, final String expressionHtml) {
		Assertion.when(ALL_RULES.containsKey(rule)).check(() -> expressionHtml.equals(ALL_RULES.get(rule)), "{0} already knowned but different", rule.toString());
		ALL_RULES.put(rule, expressionHtml);
	}

	private String readGramar(final PegRule<?> rule) {
		final String result = ALL_RULES.get(rule);
		if (result == null) {
			detectGrammar(rule);
			return readGramar(rule);
		}
		return result;
	}

	public String render(final PegRule<?> rootRule) {
		detectGrammar(rootRule);
		final Map<String, PegGrammarRule<?>> rules = new LinkedHashMap<>();
		for (final Map<String, PegGrammarRule<?>> entry : NAMED_RULES.values()) {
			rules.putAll(entry); //On resoud les conflits de noms en conservant l'ordre des profondeurs.
		}

		return rules.entrySet()
				.stream()
				.map(entry -> new StringBuilder()
						.append("<h1 id='").append(entry.getValue().getRuleName()).append("'>").append(entry.getValue().getRuleName()).append("</h1>\n")
						.append("<script>\n")
						.append("Diagram(\n\t")
						.append(readGramar(entry.getValue().getRule()))
						.append("\n).addTo();\n")
						.append("</script>\n\n")
						.toString())
				.collect(Collectors.joining());
	}

	private void detectGrammar(final PegRule<?> rule) {
		try {
			depth++;
			if (!NAMED_RULES.containsKey(depth)) {
				NAMED_RULES.put(depth, new LinkedHashMap<>());
			}
			if (rule instanceof PegChoiceRule) {
				choice((PegChoiceRule) rule);
			} else if (rule instanceof PegManyRule) {
				many((PegManyRule<?>) rule);
			} else if (rule instanceof PegGrammarRule) {
				grammar((PegGrammarRule<?>) rule);
				detectGrammar(((PegGrammarRule<?>) rule).getRule()); //on calcul la grammaire en dessous
			} else if (rule instanceof PegOptionalRule) {
				optional((PegOptionalRule<?>) rule);
			} else if (rule instanceof PegSequenceRule) {
				sequence((PegSequenceRule) rule);
			} else if (rule instanceof PegTermRule) {
				term((PegTermRule) rule);
			} else if (rule instanceof PegWhiteSpaceRule) {
				whiteSpace((PegWhiteSpaceRule) rule);
			} else if (rule instanceof PegWordRule) {
				word((PegWordRule) rule);
			} else if (rule instanceof AbstractRule) {
				detectGrammar(((AbstractRule<?, ?>) rule).getMainRule()); //on calcul la grammaire en dessous, avant de l'associer à cette règle
				populateGramar(rule, readGramar(((AbstractRule<?, ?>) rule).getMainRule()));
			} else {
				populateGramar(rule, rule.getExpression());
			}
		} finally {
			depth--;
		}

	}
}
