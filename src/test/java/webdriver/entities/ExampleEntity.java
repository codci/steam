package webdriver.entities;
import webdriver.Entity;

/** Describes the entity Example
 */
public class ExampleEntity extends Entity {

	public ExampleEntity(String string) {
		super(string);
	}

	/** All fields that are avaiable to set
	 */
	public enum Fields{
		FIELD1("Field 1"),
		FIELD2("Field 2"),
		FIELD3("Field 3");

		private String val;

		/** Set name of field
		 * @param name name of field
		 */
		Fields(String name) {
			val = name;
		}

		@Override
		public String toString() {
			return val;
		}
	}

}
