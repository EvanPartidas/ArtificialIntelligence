package NeuralNetwork;

public abstract class Neuron {
	private
	double value,error;
	double[] weights,weightDeltas;
	// @param neurons, number of neurons in next layer
	public Neuron(int neurons){
		value=0;
		error=0;
		//checking if output layer
		if(neurons>0){
			weightDeltas = new double[neurons];
			weights = new double[neurons];
			randomize();
		}
	}
	
	//Randomize weights
	public void randomize(){
		int i;
		for(i=0;i<weights.length;i++)
			weights[i]=(2*Math.random())-1;
	}
	
	//Adjusts weights and clears deltas
	// @param learning rate to be used
	public void adjust(double rate){
		for(int i=0;i<weights.length;i++){
			weights[i]+=weightDeltas[i]*rate;
			weightDeltas[i]=0;
		}
	}
	
	//Clear function for resetting value to zero
	public void clear(){
		value=0;
		error=0;
	}
	
	// @param zeta, what to set error to
	public void setError(double zeta){
		error=zeta;
	}
	// @return error
	public double getError(){
		return error;
	}
	//accumulating sum of previous neurons outputs
	public void add(double delta){
		value+=delta;
	}
	public void activate(){
		value = actFunction(value);
	}
	public double value(){
		return value;
	}
	/*
	Activation function below is meant to be able to be overridden
	in subclasses to allow for specifying functions.
	*/
	
	//Activation function to be 
	// @param zeta
	// @return zeta after going through activation function
	protected abstract double actFunction(double zeta);
	
	//Returning derivative of the activation function with input zeta
	/*	@param zeta-input value 
	 * @param active-whether or not zeta has gone through 
	 * the activation function.
	 */
	protected abstract double derivative(double zeta, boolean active);
	
	/*
	
		Pre-defined activation functions.
		To be used when actFunction() and actDeriv()
		are overriden.
		
		For Derivatives:
		@param zeta-number to be passed through
		@param active-has the number already been passed
		through the activation function
	*/
	protected static final double sigmoid(double zeta){
		  return 1.0 / (1 + Math.exp(-zeta));
	}
	protected static final double sigDeriv(double zeta, boolean active){
		return active?(zeta*(1-zeta)):sigmoid(zeta)*(1-sigmoid(zeta));
	}
	
}
