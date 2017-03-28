package webdriver.report;

public enum Errors {
	ELEMENT_ABSENT("is absent"),SERVICE_UNAVAILABLE ("SERVICE UNAVAILABLE"), SERVER_ERROR("ПРОВЕРКА"), UNKNOWN_ERROR(""), DATA_ERROR("connection refused");
	
	/**
	 * @uml.property  name="message"
	 */
	private String message;
	Errors(String message){
		this.message = message;
	}

    public String getMessage() {
        return message;
    }

    public static Errors getByExistingIn(String message) {
        for (Errors errors : Errors.values()) {
            if (message.contains(errors.getMessage())) {
                return errors;
            }
        }
        return null;
    }
}
