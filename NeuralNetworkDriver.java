package NeuralNetwork;

public class NeuralNetworkDriver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		NeuralNetwork n = new NeuralNetwork(2,1,3,1,true);
		double[][] inputs =
			{ 
				{1.0,1.0},
				{0,1.0},
				{1.0,0},
				{0,0}
			};
		double[][] target =
			{ 
				{0},
				{1},
				{1},
				{0}
			};
		for(int i=0;i<200000;i++){
			for(int j=0;j<4;j++){
				n.feed(inputs[j]);
				n.calcCost(target[j],false);
				n.learn(0.2);
			}
			System.out.print("Error: ");
			System.out.printf("%.12f \n",n.getError());
		}
		for(int j=0;j<4;j++){
			double[] result = n.feed(inputs[j]);
			for(double d:inputs[j]){
				System.out.print(d+" ");
			}
			System.out.print("=");
			for(double d:result){
				System.out.printf("%.0f ",d);
				System.out.printf(" %.5f \n",d);
			}
		}			
	}

}
