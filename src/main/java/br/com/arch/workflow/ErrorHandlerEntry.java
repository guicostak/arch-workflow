package br.com.arch.workflow;

record ErrorHandlerEntry<C>(Class<? extends Exception> exceptionType, ErrorHandler<? extends Exception, C> handler) {

    @SuppressWarnings("unchecked")
    boolean handle(Exception exception, C context) {
        if (exceptionType.isInstance(exception)) {
            ((ErrorHandler<Exception, C>) handler).handle(exception, context);
            return true;
        }
        return false;
    }
}
