package NeuralNetwork;

public class NeuralNetwork {
	
	
	/*
	 * Note!!! When not complex both Mom and Dad must possess same structure!
	 * @param mom - mother of child
	 * @param dad - father of child
	 * @param compex - change the structure or just mix weights
	 * @return child - combination of mom and dad
	 */
	public static NeuralNetwork breed(NeuralNetwork mom, NeuralNetwork dad,boolean complex){
		
		if(complex){
			int inputs,outputs,layers,depth;
			
			inputs = randnet(mom,dad).inputs.length;
			outputs = randnet(mom,dad).outputs.length;
			layers = randnet(mom,dad).hidden.length;
			depth = randnet(mom,dad).hidden[0].length;
			NeuralNetwork child = new NeuralNetwork(inputs,layers,depth,outputs,mom.actives);
			return child;
		}
		NeuralNetwork child = new NeuralNetwork(mom.inputs.length,
				mom.hidden.length,
				mom.hidden[0].length,
				mom.outputs.length,mom.actives);
		int ind=0;
		for(Neuron n:child.inputs){
			for(int i=0;i<n.weights.length;i++){
				n.weights[i]=randnet(mom,dad).inputs[ind].weights[i];
			}
			ind++;
		}

		int in2=0;
		for(Neuron[] h:child.hidden){
			ind=0;
			for(Neuron n:h){
				for(int i=0;i<n.weights.length;i++){
					n.weights[i]=randnet(mom,dad).hidden[in2][ind].weights[i];
				}
				ind++;
			}
			in2++;
		}
		/*
		ind=0;
		for(Neuron n:child.outputs){
			for(int i=0;i<n.weights.length;i++){
				n.weights[i]=randnet(mom,dad).outputs[ind].weights[i];
			}
			ind++;
		}
		*/
		return child;
	}
	
	/*
	Choose a random neural network
	@param a-first choice
	@param b-second choice
	@return a or b, chosen randomly
	*/
	private static NeuralNetwork randnet(NeuralNetwork a, NeuralNetwork b){
		return Math.random()>=0.5?a:b;
	}
	
	/*
	 * To be used to make neuron construction
	 * much shorter
	 * 
	 */
	private Neuron constNeuron(int neurons){
		return new Neuron(neurons){
			@Override
			protected double actFunction(double zeta) {
				// TODO Auto-generated method stub
				return sigmoid(zeta);
			}
			@Override
			protected double derivative(double zeta, boolean active) {
				// TODO Auto-generated method stub
				return sigDeriv(zeta,active);
			}
		};
	}
	
	//Neural Network matrixes of all Neurons
	Neuron[] inputs, outputs;
	Neuron[][] hidden;
	boolean[] actives;
	/*
	Constructor
	@param inputs-number of inputs neurons
	@param hiddenLayers-number of hidden layers
	@param hiddenNeurons-number of neurons in each hidden layer
	@param outputs-number of output neurons 
	@param actives-boolean array of which outputs to activate
	*/
	public void construct(int inputs,int hiddenLayers,int hiddenNeurons,int outputs, boolean[] actives){
		
		
		//Initializing layer arrays
		this.inputs = new Neuron[inputs];
		this.outputs = new Neuron[outputs];
		hidden = new Neuron[hiddenLayers][hiddenNeurons];
		
		// Initializing input layer
		for(int i=0;i<inputs;i++){
			this.inputs[i]= constNeuron(hiddenNeurons);
		}
		
		// Initializing hidden layers
		for(int i=0;i<hidden.length;i++){
			for(int j=0;j<hidden[0].length;j++){
				if(i==hidden.length-1)
					hidden[hidden.length-1][j]= constNeuron(outputs);
				else
				hidden[i][j]= constNeuron(hiddenNeurons);
			}
		}
		
		//Output layer
		for(int i=0;i<outputs;i++){
			this.outputs[i]= constNeuron(0);
		}
		this.actives=actives;
	}
	//Set all actives to the value specified
	public NeuralNetwork(int inputs,int hiddenLayers,int hiddenNeurons,int outputs, boolean activate){
		boolean[] active = new boolean[outputs];
		for(int i=0;i<active.length;i++)
			active[i]=activate;
		construct(inputs, hiddenLayers, hiddenNeurons, outputs, active);
	}
	//Construct with all parameters
	public NeuralNetwork(int inputs,int hiddenLayers,int hiddenNeurons,int outputs, boolean[] activates){
		construct(inputs, hiddenLayers, hiddenNeurons, outputs, activates);
	}
	/*
	 * @param factor - mutation factor (Chance of weight being mutated)
	 */
	public void mutate(double factor){
		int i;
		for(Neuron n:inputs)
			for(i=0;i<n.weights.length;i++)
				if(Math.random()<=factor)
					n.weights[i]=(2*Math.random())-1;
		
		for(Neuron[] foo:hidden)
			for(Neuron n:foo)
				for(i=0;i<n.weights.length;i++)
					if(Math.random()<=factor)
						n.weights[i]=(2*Math.random())-1;
		/*
		for(Neuron n:outputs)
			for(i=0;i<n.weights.length;i++)
				if(Math.random()<=factor)
					n.weights[i]=Math.random();
		 */
		
	}
	
	// @param in, input array
	// @return results, the array of outputs
	public double[] feed(double[] in){
		
		//Clear data from previous run
			
			//Input
			for(Neuron n:inputs)
					n.clear();
			//Hidden
			for(Neuron[] arr:hidden)
				for(Neuron n:arr)
					n.clear();
			
			//Output
			for(Neuron n:outputs)
				n.clear();
			
		
		// Set up input layer
		for(int i=0;i<in.length;i++){
			inputs[i].add(in[i]);
		}
		
		// Input Layer to hidden layer
		for(int i=0;i<in.length;i++){
			double val = inputs[i].value();
			for(int j=0;j<hidden[0].length;j++){
				hidden[0][j].add(val*inputs[i].weights[j]);
			}
		}
		
		//Hidden layers
		for(int i=0;i<hidden.length-1;i++){
			for(int j=0;j<hidden[i+1].length;j++){
				hidden[i][j].activate();
				double val = hidden[i][j].value();
				for(int k=0;k<hidden[0].length;k++){
					hidden[i+1][k].add(val*hidden[i][j].weights[k]);
				}
			}
		}
		
		//Hidden to Output Layer
		int ind = hidden.length-1;
		for(int i=0;i<hidden[0].length;i++){
			hidden[ind][i].activate();
			double val = hidden[ind][i].value();
			for(int j=0;j<outputs.length;j++){
				outputs[j].add(val*hidden[ind][i].weights[j]);
			}
		}
		
		//Final Activation and return
		double[] results = new double[outputs.length];
		
		for(int i=0;i<outputs.length;i++){
			if(actives[i])
				outputs[i].activate();
			results[i]=outputs[i].value();
		}
		return results;
	}
	double error;
	//@return error
	public double getError(){
		double d = error;
		error=0;
		return d;
	}
	
	//Cost function for calculating error and deltas
	// @param target, array of target outputs
	// @param deltas, is the array an array of deltas?
	// @returns input error array
	public double[] calcCost(double[] target,boolean deltas){
		
		// Declare variables (To increase speed)
		int i,j,k;
		
		//Output error
		//If it is an array of deltas,
		//just pass the deltas in as the error
		if(deltas)
			for(i=0;i<outputs.length;i++)
				outputs[i].setError(target[i]);
		//If it isn't, do the standard protocal
		else
			for(i=0;i<outputs.length;i++){
				double deltaOut = actives[i]?outputs[i].derivative(outputs[i].value(), true):1;
				outputs[i].setError((target[i]-outputs[i].value())*deltaOut);
				error+=Math.pow(outputs[i].getError(),2);
			}
		
		// Last Hidden layer
		for(j=0;j<hidden[0].length;j++){
			Neuron n = hidden[hidden.length-1][j];
			double sum=0;
			for(k=0;k<outputs.length;k++){
				sum+=outputs[k].getError()*n.weights[k];
				n.weightDeltas[k]=n.value()*outputs[k].getError();
			}
			n.setError(sum*n.derivative(n.value(),true));
		}
		
		//Hidden Layers
		for(i=hidden.length-2;i>=0;i--){
			for(j=0;j<hidden[i+1].length;j++){
				Neuron n = hidden[i][j];
				double sum=0;
				for(k=0;k<hidden[0].length;k++){
					sum+=hidden[i+1][k].getError()*n.weights[k];
					n.weightDeltas[k]=hidden[i+1][k].getError()*n.value();
				}
				n.setError(sum*n.derivative(n.value(),true));
			}
		}
		
		//double array to return
		double[] inputerror=new double[inputs.length];
		
		// Input layer
		for(i=0;i<inputs.length;i++){
			Neuron n = inputs[i];
			double sum=0;
			for(j=0;j<hidden[0].length;j++){
				Neuron h = hidden[0][j];
				sum+=hidden[0][j].getError()*n.weights[j];
				n.weightDeltas[j]=h.getError()*n.value();
				
			}
			n.setError(sum);
			inputerror[i]=n.getError();
		}
		return inputerror;
	}
	
	//Make the neural network learn
	// @param learning rate to be used
	public void learn(double rate){
		int i,j;
		
		for(i=0;i<inputs.length;i++){
			inputs[i].adjust(rate);
		}
		
		for(i=0;i<hidden.length;i++){
			for(j=0;j<hidden[0].length;j++){
				hidden[i][j].adjust(rate);
			}
		}
	}
	//Reset the weight matrices
	public void reset(){
		int i,j;
		
		for(i=0;i<inputs.length;i++){
			inputs[i].randomize();
		}
		
		for(i=0;i<hidden.length;i++){
			for(j=0;j<hidden[0].length;j++){
				hidden[i][j].randomize();
			}
		}
	}
}
