package NeuralNetwork;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Simulator {
	private Panel p;
	private Thread t;
	public Simulator() throws InterruptedException{
		JFrame frame = new JFrame("N-Net Test");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setSize(1076, 760);
		p = new Panel();
		p.setSize(1076, 760);
		frame.add(p);
		frame.setVisible(true);
		t = new Thread(p);
		t.start();
		frame.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyCode()==KeyEvent.VK_C)
					p.brain();
				else if(e.getKeyCode()==KeyEvent.VK_E)
					p.flip();
				else if(e.getKeyCode()==KeyEvent.VK_R){
						t.interrupt();
						t = new Thread(p);
						p.play();
						t.start();
				}
				else if(e.getKeyCode()==KeyEvent.VK_P){
					p.pause = !p.pause;
				}
				else if(!p.useBrain()&&e.getKeyCode()==KeyEvent.VK_SPACE){
						p.jump(true);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		p.evolution();
	}
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		new Simulator();
	}

}

class Brain implements Comparable<Brain>,Runnable{
	public NeuralNetwork brain;
	public Color c;
	public Rectangle rect;
	public Thread t;
	private int vel;
	private Panel p;
	private boolean good,alive;
	private int[] color;
	private long start;
	private long fitness;
	private static int randInt(int min, int max){
		return min+(int)(Math.random()*(max-min)+1);
	}
	public Brain(NeuralNetwork brain,Panel p){
		this.brain = brain;
		color = new int[]{randInt(0,255),randInt(0,255),randInt(0,255),150};
		rect = new Rectangle(100,p.getHeight()/2-50,20,50);
		this.p=p;
	}
	public void go(){
		t = new Thread(this);
		t.start();
	}
	@Override
	public void run(){
		alive=true;
		start = System.currentTimeMillis();
		c = new Color(color[0],color[1],color[2],color[3]);
		while(!Thread.currentThread().isInterrupted()){
			if(!p.pause){
				int floor = p.getHeight()/2-50;
				double[] in = new double[]{p.dist(),p.speed(),Math.abs(rect.y-floor)};
				double[] out = brain.feed(in);
				if(out[0]>0.5)
					jump();
				if(out[1]>0.5)
					fall();
				rect.setLocation(100, Math.min(rect.y-vel,floor));
				vel=rect.y<floor?vel-1:0;
				good=rect.y==floor;
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {Thread.currentThread().interrupt();}
			}
		}
	}
	public boolean alive(){
		return alive;
	}
	public long fitness(){
		return fitness;
	}
	public void kill(){
		alive=false;
		fitness = start - System.currentTimeMillis();
		c= new Color(0,0,0,0);
		t.interrupt();
	}
	public void fall(){
		if(p.pause)
			return;
		vel=vel>-15?vel-1:vel;
	}
	public void jump(){
		if(p.pause)
			return;
		vel=good?15:vel;
		good=false;
	}
	@Override
	public int compareTo(Brain o) {
		// TODO Auto-generated method stub
		long a = fitness;
		long b = o.fitness();
		return a>b?1:(a==b?0:-1);
	}
}

@SuppressWarnings("serial")
class Panel extends JPanel implements Runnable{
	private HashSet<Thread> threads;
	private Thread ev;
	private int speed,vel;
	private ArrayList<Rectangle> c;
	private boolean good,HUD,brain,evolve;
	public boolean pause;
	public Brain[] population;
	private int size;
	private Panel me=this;
	private static int randInt(int min, int max){
		return min+(int)(Math.random()*(max-min)+1);
	}
	public Panel(){
		super(null);
		setBackground(Color.CYAN);
		threads=new HashSet<>();
		population = new Brain[20];
		population[0] = new Brain(new NeuralNetwork(3,1,4,2,true),this);
		pause = false;
		play();
	}
	public void play(){
		end();
		time=0;
		speed=1;
		vel=0;
		HUD=true;
		good=true;
		c =new  ArrayList<>();
		c.add(new Rectangle(500,330,20,20));
		c.add(new Rectangle(-100,330,20,20));
		new Thread(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!Thread.currentThread().isInterrupted()){
					repaint();
				}
			}
		}).start();
	}
	public void flip(){
		evolve=!evolve;
	}
	public void evolution(){
		evolve=true;
		if(ev!=null){
			ev.interrupt();
		}
		else{
			ev = new Thread(){
				@Override
				public void run(){
					size=0;
					for(int i=1;i<population.length;i++){
						population[i]= new Brain(new NeuralNetwork(3,1,4,2,true),me);
					}
					while(!Thread.currentThread().isInterrupted()){
						me.play();
						if(evolve){
							for(int i=0;i<population.length;i++){
								population[i].go();
								threads.add(population[i].t);
								size++;
							}
							me.pause=false;
							int i;
							//Wait for all of these boys to die
							boolean b;
							while(size>0){
								for(i=0;i<population.length;i++){
									System.out.println();
									if(!population[i].alive())
										continue;
									b = population[i].rect.intersects(c.get(0));
									if(b){
										population[i].kill();
										size--;
									}
								}
							}
							me.pause=true;
							
							//Now that they're dead, sort em
							Arrays.sort(population);
							
							
							//Next we breed
							for(i=5;i<population.length;i++){
								Brain mom = population[randInt(0,4)];
								Brain dad = population[randInt(0,4)];
								population[i]= new Brain(NeuralNetwork.breed(mom.brain, dad.brain, false),me);
								population[i].brain.mutate(0.2);
							}
							
							//Mutate worst three to keep randomness up
							for(i=population.length-5;i<population.length;i++){
								double factor = (5.1*Math.random())/10;
								population[i].brain.mutate(factor);
							}
						}
						else{
							me.pause=false;
							size=1;
							population[0].go();
							boolean b;
							while(size>0){
								System.out.println();
								b = population[0].rect.intersects(c.get(0));
								if(b){
									population[0].kill();
									size--;
								}
								
							}
							me.pause=true;
						}
					}
				}
			};
			ev.start();
		}
	}
	
	public void jump(boolean b){
		if(!b)
			return;
		if(pause)
			return;
		vel=good?15:vel;
		good=false;
	}
	public boolean useBrain(){
		return brain;
	}
	public void brain(){
		brain=!brain;
	}
	public int dist(){
		Rectangle r = c.get(0);
		return (r.x>100?r.x:getWidth())-100;
	}
	public int speed(){
		return speed;
	}
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		g.setColor(Color.cyan);
		g.fillRect(0, 0, getWidth(),getHeight());
		g.setColor(Color.gray);
		g.fillRect(0, getHeight()/2, getWidth(),getHeight()/2);
		for(Brain b:population){
			if(b==null)
				break;
			g.setColor(b.c);
			g.fillOval(100, b.rect.y, 25, 50);
		}
		g.setColor(Color.RED);
		try{
			for(Rectangle r:c){
				g.fillOval(r.x-5, getHeight()/2-25, 25, 25);
				g.fillOval(r.x-5, getHeight()/2-25, 25, 25);
			}
		}catch(Exception e){}
		g.setColor(Color.LIGHT_GRAY);
		g.fillOval(getWidth()/2-25,getHeight()/2,50,50);
		g.setColor(Color.gray);
		if(pause){
			g.fillRect(getWidth()/2-15, getHeight()/2+10, 10, 30);
			g.fillRect(getWidth()/2+5, getHeight()/2+10, 10, 30);
		}
		else{
			int x = getWidth()/2-15,y=getHeight()/2+10;
			g.fillPolygon(new int[]{x+0,x+30,x+0}, new int[]{y,y+15,y+30}, 3);			
		}
		if(HUD){
			g.setColor(Color.BLACK);
			g.setFont(new Font("TimesRoman",1,20));
			g.drawString("Control (C): "+(brain?"Neural Network":"Keyboard"), 0, 400);
			g.drawString("Best fitness"+Long.toString(population[0].fitness()), 5, 430);
			g.drawString("Alive: "+size, 5, 455);
		}
		
	}
	public void end(){
		for(Thread t:threads){
			t.interrupt();
		}
	}
	int time;
	public static int clamp(int val,int min, int max){
		return Math.max(min, Math.min(max, val));
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		time=0;
		int i;
		while(!Thread.currentThread().isInterrupted()){
			if(!pause)
				try {
					if(++time>=500){
						if(speed>4)
							speed=clamp(speed+(Math.random()>0.5?1:-1),4,10);
						else
							speed++;
						time=0;
					}
					for(i=0;i<2;i++){
						Rectangle r = c.get(i);
						if(r.getX()<100){
							c.remove(i);
							c.add(r);
						}
						r.setLocation(r.x>-100?r.x-speed:getWidth(),r.y);
					}
					Thread.sleep(10);
				} catch (InterruptedException e) {Thread.currentThread().interrupt();}
		}
	}
}