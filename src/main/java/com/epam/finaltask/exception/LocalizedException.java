package com.epam.finaltask.exception;

public abstract class LocalizedException extends RuntimeException {

        private final String key;
        private final Object[] args;

        protected LocalizedException(String msg, String key, Object... args) {
            super(msg);
            this.key = key;
            this.args = args;
        }

        public String getKey() {return key;}
        public Object[] getArgs() { return args; }
}




