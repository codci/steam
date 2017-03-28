package webdriver.stage;

public class StageController {

	private Stages currentStage;
	/**
	 * @uml.property  name="stage"
	 * @uml.associationEnd  
	 */
	public Stages getStage(){
		return currentStage;
	}
	
	/**
	 * @param stage
	 * @uml.property  name="stage"
	 */
	public void setStage(Stages stage){
		currentStage = stage;
	}
}
