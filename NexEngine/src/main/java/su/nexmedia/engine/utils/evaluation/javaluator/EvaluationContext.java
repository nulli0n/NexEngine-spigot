package su.nexmedia.engine.utils.evaluation.javaluator;

public interface EvaluationContext {

    default public IllegalArgumentException getError(String msg) {
        return new IllegalArgumentException(msg);
    }

    default public IllegalArgumentException getError(String msg, Token tok) {
        if (tok != null) {
            return getError(tok.appendTokenInfo(msg));
        }
        else {
            return getError(msg);
        }
    }
}
