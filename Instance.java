import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

public class Instance {
	public List<Permutation> A; //ensemble de depart
	
	public int n;
	public int m;
	public int nbPairs;
	
	public int[][] tabD;//distance matrix ("right" table)
	public boolean[][] tabC;//matrice de contraintes: tabC[i][j]=true == (i<j) ds contraintes
	public boolean tabCprime[][]; //copy of the constraints tabC
	//public int[][] tabPairStatus;// tabPairStatus[i-1][j-1]: i<j:  5 confirmed (constraint), 4 present in all SA median, 3 ambiguous, 2 abscent, 1 infirmed, 0?
	
	public boolean isSubInstance;
	public Instance parentInstance;
	public int[] tableSubPermu;
	public int[] tableInvertSubPermu;
	
	public int simple_lower_bound;
	public int add3cyles_lower_bound;
	
	public int SA_upper_bound;
	public int BestOfA_upper_bound;
	public int Borda_upper_bound;
	public int Copeland_upper_bound;
	public int Opt2_upper_bound;
	public int Opt3_upper_bound;
	public int Borda_opt3_upper_bound;
	public int Copeland_opt3_upper_bound;
	public Permutation Copeland_opt3_permutation;
	public int CircMvtLocalSearch_upper_bound;
	public int MedianGame_upper_bound;
	public int EucledianArccosMedianRn_upper_bound;
	
	public int natural_lower_bound;
	public Permutation lowerBoundBestOrderOfExploration;
	public int cycles3_lower_bound;
	public int cycles4_lower_bound;
	public int cyclesN_wConstraints_lower_bound;
	public int cyclesN_wConstraints_guided_lower_bound;
	public int cycles3_maxFlow_lower_bound ;
	
	public int best_lower_bound;
	public int best_upper_bound;
	public int first_gap;
	public double first_relative_gap;
	public boolean isOptimal;
	public Set<Permutation> Medians; //ensemble mediane de A
	
	public int cplexMIPresult;
	public double cplexLPresult;
	
	//stats
	public int minDist;
	public int maxDist;
	public double avgDist;
	public double minDist_normalized;
	public double maxDist_normalized;
	public double avgDist_normalized;
	
	
	
	//for branch and bound
	public boolean[][] tabDuos;//matrice de contraintes par duos, equivalents des contraintes G/D, permet de savoir si x,y est permis dans la solution optimale
	public boolean[][][] tabTriplets;//matrice de contraintes par triplet, permet de savoir si x,y,z est permis dans la solution optimale
	public List<HashSet<Integer>> contraintesG;
	public List<HashSet<Integer>> contraintesD;
	public boolean[][] tabContraintesG;
	public boolean[][] tabContraintesD;
	public HashMap<BitVector,Integer> topScores;//top scores ascending
	public HashMap<BitVector,Integer> subScores;//sub scores
	public boolean[][] permissionSpatiale;
	public int nbExploredNodesBnB=0;
	public int nbMaxNodesBnB=0;
	
	public int[][][] tabTriangleAdd;
	public int[][] tabNbTriangleAssocie;
	public List<Integer>[][] tabTriangleAssocie;
	public int[] apport;
	//MOT3e
	public boolean[][] toCheck;
	public int[][] tabDelta;
	public int[][][] tabDELTA ;
	public int[][] tabMaxDELTA ;
	//fin MOT3e
	public double nbRejectGD=0.0;
	public double nbRejectTriplets=0.0;
	public double nbRejectMOT=0.0;
	public double nbRejectMOT4=0.0;
	public double nbRejectSemiDistBI=0.0;
	public double nbRejectSemiDistBIadd=0.0;
	public double nbRejectTopScores=0.0;
	public double nbRejectSubScores=0.0;
	public double nbRejectSpatial=0.0;
	public double nbReject=0.0;
	
	
	
	
	//for elections names
	public List<String> rankingCandidatesNames;
	//public List<String> rankingNames;

	//empty constructor, usually we call a read file method after
	public  Instance(){
		A = new ArrayList<Permutation>();
		
		//beginningThinks();
	}
	
	private void beginningThinks() {
		// TODO Auto-generated method stub
		isSubInstance = false;
		m = A.size();
		n=pickARandom().getSize();
		nbPairs = (n*(n-1))/2;
		creerTabD();
		SA_upper_bound = -1;
		BestOfA_upper_bound = -1;
		Borda_upper_bound = -1;
		Copeland_upper_bound = -1;
		Opt2_upper_bound = -1;
		Opt3_upper_bound = -1;
		Borda_opt3_upper_bound = -1;
		Copeland_opt3_upper_bound = -1;
		Copeland_opt3_permutation = null;
		CircMvtLocalSearch_upper_bound = -1;
		MedianGame_upper_bound = -1;
		natural_lower_bound= -1;
		lowerBoundBestOrderOfExploration = null;
		cycles3_lower_bound= -1;
		cycles4_lower_bound= -1;
		cyclesN_wConstraints_lower_bound= -1;
		cyclesN_wConstraints_guided_lower_bound= -1;
		cycles3_maxFlow_lower_bound = -1;
		best_lower_bound = -1;
		best_upper_bound = -1;
		simple_lower_bound = -1;
		add3cyles_lower_bound = -1;
		isOptimal = false;
		Medians = null;
		tabC = new boolean[n][n];
		for (int i = 1; i<= n; i++){
			for (int j = 1; j<= n; j++){
				tabC[i-1][j-1] = false;
			}
		}
		nbExploredNodesBnB = 0;
		
		cplexMIPresult = 0;
		cplexLPresult =0.0;
		
		//stats
		int dist = 0;
		minDist=999999999;
		maxDist=0;
		avgDist=0.0;
		for(Permutation pi1 : A){
			for(Permutation pi2 : A){
				if (pi1 != pi2){
					dist = pi1.distanceTo(pi2);
					if (dist < minDist){
						minDist = dist;
					}
					if (dist > maxDist){
						maxDist = dist;
					}
					avgDist += (double)dist;
				}
			}
		}
		avgDist /= 2.0;
		avgDist /= (double)(m*(m-1)/2);
		
		minDist_normalized = minDist/(double)(n*(n-1)/2);
		maxDist_normalized = maxDist/(double)(n*(n-1)/2);
		avgDist_normalized = avgDist/(double)(n*(n-1)/2);
	}

	//constructor from a set of permutations
	public  Instance(Set<Permutation> SetPermu){
		A = new ArrayList<Permutation>();
		for (Permutation pi : SetPermu){
			A.add(pi);
		}

		beginningThinks();
	}

	//constructor from a list of permutations
	public  Instance(List<Permutation> SetPermu){
		A = new ArrayList<Permutation>();
		for (Permutation pi : SetPermu){
			A.add(pi);
		}

		beginningThinks();
	}

	//not working
	//Mallows model with dispertion parameter theta
	public  Instance(int nbPermu, int nbElements, double theta){

		A = new ArrayList<Permutation>();
		nbPairs = nbElements*(nbElements-1)/2;
		Permutation central = createARandom(nbElements);
		int dist = 0;
		double prob = 0.0;
		double sum = 0.0;
		double[] probabilities = new double[nbPairs+1];
		System.out.println("probs");
		for (int i=0; i<= nbPairs; i++){
			probabilities[i]=Math.exp(-i*theta);
			sum += Math.exp(-i*theta);
			System.out.println(i+"\t"+probabilities[i]);
		}
		System.out.println("probs unif.");
		for (int i=0; i<= nbPairs; i++){
			probabilities[i]/=sum;
			System.out.println(i+"\t"+probabilities[i]);
		}
		System.out.println("");

		Permutation pi =  null;
		while (A.size() < nbPermu){
			pi = createARandom(nbElements);
			//if (!A.contains(pi)) A.add(pi);
			prob = Math.random();
			dist = pi.distanceTo(central);
			//System.out.println(dist + " "+ nbPairs);
			//System.out.println(prob + " vs "+ probabilities[dist]);
			if (prob < probabilities[dist]) {
				A.add(pi);
				System.out.println(A.size());
			}

		}

		beginningThinks();
	}

	//random uniform model
	public  Instance(int nbPermu, int nbElements){
		A = new ArrayList<Permutation>();

		Permutation pi =  null;
		while (A.size() < nbPermu){
			pi = createARandom(nbElements);
			//if (!A.contains(pi)) A.add(pi);
			A.add(pi);
		}

		beginningThinks();
	}
	
	
	/*
	 * example {[7,1,5,4,9,8,3,2,10,6],[6,8,4,10,5,7,1,2,9,3],[2,1,8,5,7,4,10,3,9,6]}
	 */
	public Instance(String line){
		char u;
		int depth=0;
		String nombreString = "";
		int[] a = null;
		List<Integer> permutationInProgress = null;
		Permutation pi;
		
		//A = new ArrayList<Permutation>();
		if (line.length()!=0 ){
			for (int i=0;i< line.length();i++){
				u=line.charAt(i);
				//System.out.println(u);
				if (u == '{' || u == '['){
					depth++;
					if (depth ==1){
						A = new ArrayList<Permutation>();
					}else if (depth ==2){
						pi=null;
						permutationInProgress= new ArrayList<Integer>();
						nombreString="";
					}else
						System.out.println("erreur de lecture");
						
				}
				else if (u == '}' || u == ']'){
					depth--;
					if (depth ==0){
						
						
						//Instance myInstance =new Instance(A);
						beginningThinks();
					
						
					}else if (depth ==1){
						permutationInProgress.add(Integer.parseInt(nombreString));
						a = new int[permutationInProgress.size()];
						for (int j=0; j<permutationInProgress.size();j++ )
							a[j]= permutationInProgress.get(j);
						pi = new Permutation(a);
						A.add(pi);
					}else if (depth ==2){
						permutationInProgress.add(Integer.parseInt(nombreString));
					}else
						System.out.println("erreur de lecture");
				}
				else if (u == ','){
					if (depth== 2){
						permutationInProgress.add(Integer.parseInt(nombreString));
						nombreString="";
					}
					else{
						//nothing
					}
				}
				else{
					if (depth== 2){
						nombreString += u;
					}
				}
				
			}
		
		}
			
	}
	

	
	
	public void creerTabD(){
		tabD = new int [n][n];
		int aGauche=0,aDroite=0;
		for (int i=1;i<=n;i++){
			for (int j=1; j<=n;j++){
				if (i != j){
					aGauche=0;
					aDroite=0;
					for (Permutation p: A){
						if (p.getPosition(j) < p.getPosition(i)){
							aGauche++;
						}
						else{
							aDroite++;
						}
					}
					tabD[j-1][i-1]=aDroite;
				}
			}	
		}
	
	}	
	
	public void printTabD(){
		System.out.println("tabD: ");
		for (int i=0; i<n;i++){
			for (int j=0; j<n;j++){
				System.out.print(tabD[j][i]+"  ");
			}
			System.out.println("");
		}
		
	}
	
	public void giveConstraints(boolean[][] tabConstraints){
		tabC = new boolean[n][n];
		for (int i = 1; i<= n; i++){
			for (int j = 1; j<= n; j++){
				tabC[i-1][j-1] = tabConstraints[i-1][j-1];
			}
		}
	}
	
	
	//pick a random permutations in the set of permutations
	public Permutation pickARandom(){
		Permutation k2 = null;
		int num=0,count=0;
		num = (int)(Math.random()*m+1);
		for (Permutation k: A){
			count++;
			if (count == num){
				k2 =  new Permutation(k.getTab());
				return k2;
			}
		}
		System.out.println("problem #201");
		return k2;
	}
	
	//pick a random permutation in the set of medians
	public Permutation pickARandomMedian(){
		Permutation k2 = null;
		int num=0,count=0;
		num = (int)(Math.random()*Medians.size()+1);
		for (Permutation k: Medians){
			count++;
			if (count == num){
				k2 =  new Permutation(k.getTab());
				return k2;
			}
		}
		System.out.println("problem #201");
		return k2;
	}
	
	//fisher-yates shuffle to create random uniform permutation
	public Permutation createARandom(int n){
		Permutation k2 = null;
		int num=0,temp=0;
		int a[] = new int[n];

		for (int i=1;i<=n;i++){
			a[i-1]=i;
		}

		//Fisher-Yates shuffle
		for (int i=1;i<=n;i++){
			num = (int)(Math.random()*i+1);
			temp=a[num-1];
			a[num-1]=a[i-1];
			a[i-1]=temp;
		}
		
		//finaliser
		k2 =  new Permutation(a);
		
		return k2;
	}
		

	
	public void print(){
		System.out.println("MultiSet A (" + A.size() + ") :" );
		for (Permutation p: A){
			System.out.println(p);
		}
	}
	//print set of medians
	public void printM (int threshold ){
		
		
		//if (isSubPermu){
		if (isSubInstance){
			for (Permutation pi : Medians){//si subPermu
				pi.setSubPermu(tableSubPermu);
			}
		}
		
		if (isOptimal){
			System.out.println("Set M (" + Medians.size() + ") :" );
			System.out.println("distMediane= " + best_upper_bound);
		}else{
			System.out.println("Set ~M (" + Medians.size() + ") :" );
			System.out.println("distApproxMediane= " + best_upper_bound);
		}
		
		if (Medians.size() <= threshold){
			for (Permutation p: Medians){
				//System.out.println(p + "  stabilite: " + evalStabilitePermu(A.size(), p,false));
				//System.out.println(p + " "+v.verifie(p));
				System.out.println(p);
				//System.out.println(p + " " + p.distanceToSetMatrix(tabD) + " check:"+check(p,tabD));
				//System.out.println(p + " " + p.distanceToSet(A));
				//System.out.println(p + " " + p.distanceToSetMatrix(tabD));
				//System.out.println(p.getCycles());
			}
		}else{
			System.out.println("More than "+threshold+" medians...(won't print everything)");
			System.out.println("sample:");
			System.out.println(pickARandomMedian());
		}

	}
	

	
	public void writeCplexLPIntoFile(String fichier, boolean writeConstraints){
		boolean appendMode = false;
		int compteur =0;
		
		//folder check
		File f = new File(fichier);
		if (!f.exists()) {
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			
		} else{
			//nothing
		}
		

		//file
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fichier,appendMode);//file access on append mode
			BufferedWriter out = new BufferedWriter(fstream);
			String line = "";
			
			out.write("Minimize "+"\n");
			out.write(" obj: ");
			
			line = "";
			for(int i=1;i<=n;i++){
				for(int j=1;j<=n;j++){
					if (i != j){
						line += tabD[i-1][j-1] + " x_"+i+"_"+j+ " + ";
						
						if((i==n) && (j ==(n-1))){
							line = line.substring(0, line.length()-2);
							out.write(line);
							out.write("\n");
							line = "";
						}else{
							out.write(line);
							out.write("\n");
							line = "";
						}
					}
					
				}
				
			}
			
			out.write("Subject To "+"\n");
			compteur =0;
			for(int i=1;i<=n;i++){
				for(int j=i+1;j<=n;j++){
					if (writeConstraints){
						if (tabC[i-1][j-1]){
							out.write(" c"+compteur+": x_"+i+"_"+j+" = 1 \n");
						}else if (tabC[j-1][i-1]){
							out.write(" c"+compteur+": x_"+j+"_"+i+" = 1 \n");
						}else{
							out.write(" c"+compteur+": x_"+i+"_"+j+" + x_"+j+"_"+i+" = 1 \n");
						}
					}else{
						out.write(" c"+compteur+": x_"+i+"_"+j+" + x_"+j+"_"+i+" = 1 \n");
					}
					compteur++;
				}
			}
			
			
			for(int i=1;i<=n;i++){
				for(int j=1;j<=n;j++){
					if (i !=j){
						for(int k=1;k<=n;k++){
							if (k != i && k != j){
								out.write(" c"+compteur+": x_"+i+"_"+j+" + x_"+j+"_"+k+" + x_"+k+"_"+i+" >= 1 \n");
								compteur++;
							}
						}
					}
					
				}
			}
			

			
			//out.write("Bounds "+"\n");
			
			out.write("Binary "+"\n");
			for(int i=1;i<=n;i++){
				for(int j=1;j<=n;j++){
					if (i != j){
						out.write(" x_"+i+"_"+j+"\n");
					}
				}
			}
			out.write("End "+"\n");
			
			//Close the output stream
			out.close();
			fstream.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		
		
		

		
	}
	
	

	
	public void writeCplexMSTIntoFile(String fichier){
		boolean appendMode = false;
		Permutation MSTsolution = pickARandomMedian();
		int MSTscore = MSTsolution.distanceToSetMatrix(tabD);
		System.out.println("Writing Cplex solution into MST file (score="+MSTscore+")");
		
		//folder check
		File f = new File(fichier);
		if (!f.exists()) {
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			
		} else{
			//rien
		}
		
		
		//file
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fichier,appendMode);//file access on append mode
			BufferedWriter out = new BufferedWriter(fstream);
			String line = "";
			
			
			out.write("<?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +"\n");

			

			out.write("<CPLEXSolutions version=\"1.2\">" +"\n");
			out.write(" <CPLEXSolution version=\"1.2\">" +"\n");
			
			out.write("  <header" +"\n");
			
			out.write("   problemName=\"ex_2.lp\"" +"\n");
			out.write("   solutionName=\"m1\"" +"\n");
			out.write("   solutionIndex=\"0\"" +"\n");
			out.write("   MIPStartEffortLevel=\"0\"" +"\n");
			out.write("   writeLevel=\"2\"" +"/>\n");
			
			
			out.write("  <variables>" +"\n");
			
			
			
			
			int compteur =0;
			for(int i=1;i<=n;i++){
				for(int j=i+1;j<=n;j++){
					
					out.write("   <variable name=\""+ "x_"+i+"_"+j+"\" ");
					out.write("index=\""+compteur+"\" ");
					if (MSTsolution.getPosition(i) < MSTsolution.getPosition(j)){
						out.write("value=\""+1+"\" ");
					}else{
						out.write("value=\""+0+"\"");
					}
					
					
					
					out.write("/>\n");
					compteur++;
				}
			}
			
			out.write("  </variables>" +"\n");
			out.write(" </CPLEXSolution>" +"\n");
			out.write("</CPLEXSolutions>" +"\n");
			




			//Close the output stream
			out.close();
			fstream.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		//System.out.println("Done");
		
		
	}
	
	
	
	public void writeResultIntoFile(String fichier){
		boolean appendMode = false;
		
		//folder check
		File f = new File(fichier);
		if (!f.exists()) {
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			
		} else{
			//nothing
		}
		
		
		//file
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fichier,appendMode);//file access on append mode
			BufferedWriter out = new BufferedWriter(fstream);
			 
			out.write("m: "+ m+"\n");
			out.write("n: "+ n+"\n");
			out.write("\n");
			out.write("minDist: "+ minDist+"\n");
			out.write("maxDist: "+ maxDist+"\n");
			out.write("avgDist: "+ avgDist+"\n");
			out.write("minDist_normalized: "+ minDist_normalized+"\n");
			out.write("maxDist_normalized: "+ maxDist_normalized+"\n");
			out.write("avgDist_normalized: "+ avgDist_normalized+"\n");
			out.write("\n");
			out.write("BestOfA_upper_bound: "+ BestOfA_upper_bound+"\n");
			out.write("Borda_upper_bound: "+ Borda_upper_bound+"\n");
			out.write("Copeland_upper_bound: "+ Copeland_upper_bound+"\n");
			out.write("Opt2_upper_bound: "+ Opt2_upper_bound+"\n");
			out.write("Opt3_upper_bound: "+ Opt3_upper_bound+"\n");
			out.write("Borda_opt3_upper_bound: "+ Borda_opt3_upper_bound+"\n");
			out.write("Copeland_opt3_upper_bound: "+ Copeland_opt3_upper_bound+"\n");
			out.write("CircMvtLocalSearch_upper_bound: "+ CircMvtLocalSearch_upper_bound+"\n");
			out.write("MedianGame_upper_bound: "+ MedianGame_upper_bound+"\n");
			out.write("EucledianArccosMedianRn_upper_bound: "+ EucledianArccosMedianRn_upper_bound+"\n");
			out.write("SA_upper_bound: "+ SA_upper_bound+"\n");
			out.write("\n");
			out.write("natural_lower_bound: "+ natural_lower_bound+"\n");
			out.write("cycles3_lower_bound: "+ cycles3_lower_bound+"\n");
			out.write("cycles4_lower_bound: "+ cycles4_lower_bound+"\n");
			out.write("cyclesN_wConstraints_lower_bound: "+ cyclesN_wConstraints_lower_bound+"\n");
			out.write("cyclesN_wConstraints_guided_lower_bound: "+ cyclesN_wConstraints_guided_lower_bound+"\n");
			out.write("\n");
			out.write("first_gap: "+ first_gap+"\n");
			out.write("first_relative_gap: "+ first_relative_gap+"%\n");
			out.write("\n");
			out.write("best_upper_bound: "+ best_upper_bound+"\n");
			out.write("best_lower_bound: "+ best_lower_bound+"\n");
			out.write("gap: "+ getGap()+"\n");
			out.write("relative gap: "+ getRelativeGap()+"%\n");
			out.write("nbExploredNodesBnB: "+ nbExploredNodesBnB+"\n");
			out.write("nbMaxNodesBnB: "+ nbMaxNodesBnB+"\n");
			out.write("isOptimal: "+ isOptimal+"\n");
			if (Medians != null){
				if (Medians.size() >0){
					if (isOptimal){
						out.write("Median: "+ pickARandomMedian()+"\n");
						out.write("Median size: "+ Medians.size()+"\n");
					}else{
						out.write("~Median: "+ pickARandomMedian()+"\n");
						out.write("~Median size: "+ Medians.size()+"\n");
					}
				}
			}
			
			out.write("\n");
			
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		//System.out.println("Done");
		
		
	}
	
	
	
	
	
	
	
	//printing the set of permutations
	public String linearStringA (){
		String result="{";
		//printing
		for (Permutation p: A){
			result = result + p.toString() + ",";
		}
		if (result.length() > 0)
			result=result.substring(0, result.length()-1);
		result+="}";
		return result;
	}
	
	
	public void writeIntoFile(String fichier, boolean appendMode){
		
		//folder check
		File f = new File(fichier);
		if (!f.exists()) {
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			
		} else{
			//rien
		}
		
		
		//working with file
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fichier,appendMode);//file access on append mode
			BufferedWriter out = new BufferedWriter(fstream);
			 
			
			//out.write(linearStringA());
			int count = 0;
			String resultat="{";
			//printing
			for (Permutation p: A){
				count++;
				if (count != m){
					resultat = resultat + p.toString() + ",";
				}else{
					resultat = resultat + p.toString() + "}";
				}
				out.write(resultat+"\n");
				resultat="";
			}
			
			out.write("\n");
			
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		//System.out.println("Done");
		
		
	}


	public void writeElectionIntoFile(String fichier, boolean appendMode){
		
		//folder check
		File f = new File(fichier);
		if (!f.exists()) {
			if (!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			
		} else{
			//nothing
		}
		
		
		//traitement du fichier
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fichier,appendMode);//file access on append mode
			BufferedWriter out = new BufferedWriter(fstream);
			 
			
			//out.write(linearStringA());
			int count = 0;
			String resultat="";
			//affichage
			for (Permutation p: A){
				count++;
				resultat = p.toElectionString();
				out.write(resultat+"\n");
				resultat="";
			}
			
			out.write("\n");
			
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		//System.out.println("Done");
		
		
	}
	
	//first call the constructor then this, in the main code
	//has to be all in one line only for now (to be correct eventually)
	
	//example {[7,1,5,4,9,8,3,2,10,6],[6,8,4,10,5,7,1,2,9,3],[2,1,8,5,7,4,10,3,9,6]}
	//      001222222222222222222222112222222222222222222221122222222222222222222210
	//        Ap                    p p                    p p                    p*
	//           n n n n n n n n  n n   n n n  n n n n n n n   n n n n n n  n n n n
	public void loadFromFile(String fichier, int mode){

		char u;
		int profondeur=0;
		String nombreString = "";
		int[] a = null;
		List<Integer> permuEnCours = null;
		Permutation pi;
		
		//reading
        FileReader reader ;
        try{
        	reader = new FileReader(fichier);
        	Scanner in = new Scanner(reader);
        	while (in.hasNext()){
        		String line = in.nextLine();
        		//Integer.parseInt(line);
        		//line.charAt(i);
        		if (line.length()!=0 ){
        			for (int i=0;i< line.length();i++){
        				u=line.charAt(i);
        				//System.out.println(u);
        				if (u == '{' || u == '['){
        					profondeur++;
        					if (profondeur ==1){
        						A = new ArrayList<Permutation>();
        					}else if (profondeur ==2){
        						pi=null;
        						permuEnCours= new ArrayList<Integer>();
        						nombreString="";
        					}else
        						System.out.println("lecture error");
        						
        				}
        				else if (u == '}' || u == ']'){
        					profondeur--;
        					if (profondeur ==0){
        						
        						
        						//Instance myInstance =new Instance(A);
        						beginningThinks();
    						
        						
        					}else if (profondeur ==1){
        						permuEnCours.add(Integer.parseInt(nombreString));
        						a = new int[permuEnCours.size()];
        						for (int j=0; j<permuEnCours.size();j++ )
        							a[j]= permuEnCours.get(j);
        						pi = new Permutation(a);
        						A.add(pi);
        					}else if (profondeur ==2){
        						permuEnCours.add(Integer.parseInt(nombreString));
        					}else
        						System.out.println("lecture error");
        				}
        				else if (u == ','){
        					if (profondeur== 2){
        						permuEnCours.add(Integer.parseInt(nombreString));
        						nombreString="";
        					}
        					else{
        						//nothing
        					}
        				}
        				else{
        					if (profondeur== 2){
        						nombreString += u;
        					}
        				}
        				
        			}
        		
        		}
        		
        	}
        	in.close();
        	
	    } catch (FileNotFoundException e){
	        System.out.println("File "+ fichier +" not found");
	    }
	}

	//first call the constructor then this, in the main code
	// .soc
	public void loadFromFilePrefLibFormat(String fichier){

		int sizeOfPermutations = 0;
		String[] candidatesNames= new String[10];
		int candidatesCount = 0;
		int step = 0;//0:number,1:list of candidates,2:stats,3:permutations
		char u;
		String nombreString = "";
		int[] a = null;
		List<Integer> permuEnCours = null;
		Permutation pi;
		boolean firstValue;
		int permutationRepetition = 0;

		//reading
		FileReader reader ;
		try{
			reader = new FileReader(fichier);
			Scanner in = new Scanner(reader);

			A = new ArrayList<Permutation>();
			while (in.hasNext()){
				String line = in.nextLine();
				if (line.length()!=0 ){


					if (step==0){
						sizeOfPermutations=Integer.parseInt(line);
						step++;
						candidatesNames = new String[sizeOfPermutations];
					}else if (step == 1){
						candidatesNames[candidatesCount] = line;
						candidatesCount++;
						if (candidatesCount == sizeOfPermutations){
							step++;
						}
					}else if (step == 2){
						//nothing
						step++;
					}else if (step == 3){
						firstValue = true;
						pi=null;
						permuEnCours= new ArrayList<Integer>();
						nombreString="";
						for (int i=0;i< line.length();i++){
							u=line.charAt(i);
							if (u == ','){
								if (firstValue){
									permutationRepetition=Integer.parseInt(nombreString);
									nombreString="";
									firstValue = false;
								}else{
									permuEnCours.add(Integer.parseInt(nombreString));
									nombreString="";
								}

							}else{
								nombreString += u;
							}
						}
						permuEnCours.add(Integer.parseInt(nombreString));
						for (int i=0;i<permutationRepetition;i++){
							a = new int[permuEnCours.size()];
							for (int j=0; j<permuEnCours.size();j++ )
								a[j]= permuEnCours.get(j);
							pi = new Permutation(a);
							A.add(pi);
						}

					}

				}
			}
			beginningThinks();



			in.close();

		} catch (FileNotFoundException e){
			System.out.println("File "+ fichier +" not found");
		}
	}
	
	//first call the constructor then this, in the main code
	public void loadElectionFromFile(String fichier){

		char u;
		String nombreString = "";
		int[] a = null;
		List<Integer> permuEnCours = null;
		Permutation pi;
		
		//reading
        FileReader reader ;
        try{
        	reader = new FileReader(fichier);
        	Scanner in = new Scanner(reader);
        	
        	A = new ArrayList<Permutation>();
        	while (in.hasNext()){
        		String line = in.nextLine();
        		if (line.length()!=0 ){
        			pi=null;
					permuEnCours= new ArrayList<Integer>();
					nombreString="";
					for (int i=0;i< line.length();i++){
						u=line.charAt(i);
						if (u == '>'){
    						permuEnCours.add(Integer.parseInt(nombreString));
    						nombreString="";
        				}else{
        					nombreString += u;
        				}
					}
					permuEnCours.add(Integer.parseInt(nombreString));
					a = new int[permuEnCours.size()];
					for (int j=0; j<permuEnCours.size();j++ )
						a[j]= permuEnCours.get(j);
					pi = new Permutation(a);
					A.add(pi);
        		}
        	}
        	beginningThinks();
        	
        	
        	
        	in.close();
        	
	    } catch (FileNotFoundException e){
	        System.out.println("File "+ fichier +" not found");
	    }
	}
	
	//procedure that converts an election format into a set of permutations, with projection method
	/*
	 *  pomme>orange>banane>poire>toamte>broccoli>ananas>grenade>fraise
	 *  poire>tomate>pamplemousse>orange>pomme>bleuet
	 *  pomme>poire>orange>citron>tomate
	 * 
	 *  1>2>0>3>4>0>0>0>0
	 *  3>4>0>2>1>0
	 *  1>3>2>0>4
	 *  
	 *  [1,2,3,4]
	 *  [3,4,2,1]
	 *  [1,3,2,4]
	 * 
	 */
	public void convertElectionsToPermutations(String fichier){
		Map<String, Integer> candidats = new TreeMap<String, Integer>();
		rankingCandidatesNames = new ArrayList<String>();
		A = new ArrayList<Permutation>();
		int n =0;
		int m =0;
		char u;
		String candidat ="";
		boolean ok = false;
		//reading
        FileReader reader ;
        try{
        	reader = new FileReader(fichier);
        	Scanner in = new Scanner(reader);
        	
        	while (in.hasNext()){
        		String line = in.nextLine();
        		if (line.length()!=0 ){
        			//new ordering
        			m++;
        			for (int i =0; i< line.length(); i++){
        				u=line.charAt(i);
        				if (u == '>'){
        					if (candidats.get(candidat) != null){
        						int value = candidats.get(candidat);
        						candidats.put(candidat,value+1);
        					}else{
        						candidats.put(candidat,1);
        					}
        					candidat ="";
        				}else if (i == line.length()-1){
        					candidat =candidat + u;
        					//System.out.println(candidat);
        					if (candidats.get(candidat) != null){
        						int value = candidats.get(candidat);
        						candidats.put(candidat,value+1);
        					}else{
        						candidats.put(candidat,1);
        					}
        					candidat ="";
        				}else{
        					candidat =candidat + u;
        				}
        			}
        			
        		}
        	}			
        	ok = true;
	        //reader.close();
            in.close();
        
        }catch (FileNotFoundException e){
    	    System.out.println("File "+ fichier +" not found");
    	}
        
        if (ok){
        	int ID =1;
        	for (String candidat2 : candidats.keySet()){
        		if (candidats.get(candidat2) == m){
        			candidats.put(candidat2,ID);
        			ID++;
        			rankingCandidatesNames.add(candidat2);
        		}else{
        			candidats.put(candidat2,0);
        		}
        	}
        	n = ID -1;
        	
        	
        	//2nd lecture
            try{
            	reader = new FileReader(fichier);
            	Scanner in = new Scanner(reader);
            	int t[]= new int[n];
            	int compteur =0;
            	
            	while (in.hasNext()){
            		String line = in.nextLine();
            		if (line.length()!=0 ){
            			t= new int[n];
            			compteur =0;
            			//new ordering
            			for (int i =0; i< line.length(); i++){
            				u=line.charAt(i);
            				if (u == '>'){
            					if (candidats.get(candidat) != 0){
            						t[compteur] = candidats.get(candidat);
                        			compteur++;
            						//System.out.print(" " + candidats.get(candidat));
            					}
            					candidat ="";
            				}else if (i == line.length()-1){
            					candidat =candidat + u;
            					if (candidats.get(candidat) != 0){
            						t[compteur] = candidats.get(candidat);
                        			compteur++;
            						//System.out.print(" " + candidats.get(candidat));
            					}
            					candidat ="";
            				}else{
            					candidat =candidat + u;
            				}
            			}
            		Permutation pi = new Permutation(t);
            		//System.out.println(pi);
            		A.add(pi);
            		}
            		//System.out.println("");
            	}			
            	ok = true;
    	        //reader.close();
                in.close();
            
            }catch (FileNotFoundException e){
        	    System.out.println("File "+ fichier +" not found");
        	}
        }
        
        
        beginningThinks();
	        		
	}
	
	public void writeElectionResults(String fichier){
		String result = "";
		Permutation finalOrder = pickARandomMedian();
		try{
			// Create file 
			FileWriter fstream = new FileWriter(fichier,false);
			BufferedWriter out = new BufferedWriter(fstream);
			
			result = rankingCandidatesNames.get(finalOrder.getTab()[0]-1);
			for (int candidateNumber = 1; candidateNumber < rankingCandidatesNames.size(); candidateNumber++){
				result += ">"+ rankingCandidatesNames.get(finalOrder.getTab()[candidateNumber]-1);
			}
			out.write(result);
			out.write("\n");
			//Close the output stream
			out.close();
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	public void setLowerBound(int bound){
		if  (bound > best_lower_bound){
			best_lower_bound = bound;
			if (best_lower_bound == best_upper_bound){
				isOptimal = true;
			}
		}
	}
	public void setUpperBound(int bound){
		if (best_upper_bound == -1){
			best_upper_bound = bound;
			if (best_lower_bound == best_upper_bound){
				isOptimal = true;
			}
		}
		else if  (bound < best_upper_bound){
			best_upper_bound = bound;
			if (best_lower_bound == best_upper_bound){
				isOptimal = true;
			}
		}
	}
	public void addSolverPermutations(Set<Permutation> solverPermutations){
		if (Medians != null){
			Medians.addAll(solverPermutations);
		}else{
			Medians = new HashSet<Permutation>();
			Medians.addAll(solverPermutations);
		}
	}
	public void addSolverPermutation(Permutation solverPermutation){
		if (Medians != null){
			Medians.add(solverPermutation);
		}else{
			Medians = new HashSet<Permutation>();
			Medians.add(solverPermutation);
		}
	}
	
	public void trimMedianSet (){
		int distCurrent=0;
		Set<Permutation> trimmedMedians = new HashSet<Permutation>();
		for (Permutation p: Medians){
			distCurrent=p.distanceToSetMatrix(tabD);
			if (distCurrent == best_upper_bound){
				trimmedMedians.add(p);
			}
			else if (distCurrent < best_upper_bound){
				//nothing
				System.out.println("error 23456843645");
			}
			else{
				//nothing
			}	
			
		}
		
		Medians.clear();
		Medians.addAll(trimmedMedians);
		
		
		
		
	}
	
	public void setMedianDist(int medianDist){
		best_lower_bound = medianDist;
		best_upper_bound = medianDist;
		isOptimal = true;
	}
	public void decalreIsOptimal(){
		best_lower_bound = best_upper_bound;
		isOptimal = true;
	}
	/*public void setApproxPermutationst(Set<Permutation> ApproxP){
		ApproxPermutations = ApproxP;
	}*/
	public void computeFirstGap(){
		int gap = best_upper_bound -best_lower_bound;
		double relativeGap = 100.0*(best_upper_bound -best_lower_bound)/best_lower_bound;
		first_gap = gap;
		first_relative_gap = relativeGap;
	}
	public int getGap(){
		int gap = best_upper_bound -best_lower_bound;
		return gap;
	}
	public double getRelativeGap(){
		//int gap = best_upper_bound -best_lower_bound;
		double relativeGap = 100.0*(best_upper_bound -best_lower_bound)/best_lower_bound;
		return relativeGap;
	}
	
	//subInstance
	//create a permutations set from a sub set of elements, by projections
	public Instance creerSubInstance (int elements[]){
		Instance subInstance = null;
		
		int sizeSubInstance = elements.length;
		List<Permutation> SubA = new ArrayList<Permutation>();
		
		int[] newTableSubPermu = new int[sizeSubInstance]; //0-->13, 1-->24,...
		int[] newTableInvertSubPermu = new int[n];//0-->0,1-->0,...,12-->1,...,23-->2,...

		for (int i = 0; i < n; i++){
			newTableInvertSubPermu[i] = 0;
		}
		for (int i = 0; i < sizeSubInstance; i++){
			newTableSubPermu[i] = elements[i];
			newTableInvertSubPermu[elements[i]-1] = i+1;
		}
		
		
		for (Permutation pi : A){
			List<Integer> permuEnCours  = new ArrayList<Integer>();
					
			for (int i = 0; i < n; i++){
				if (newTableInvertSubPermu[pi.getTab()[i]-1] != 0){
					permuEnCours.add(newTableInvertSubPermu[pi.getTab()[i]-1]);
				}
			}
					
			
			Permutation piSub = null;
			int[] b = new int[permuEnCours.size()];
			for (int j=0; j<permuEnCours.size();j++ )
				b[j]= permuEnCours.get(j);
			piSub = new Permutation(b);
			
			piSub.setSubPermu(newTableSubPermu);
			
			
			SubA.add(piSub);
		}
		
		subInstance = new Instance(SubA);
		subInstance.setAsSubInstanceOf(this,newTableSubPermu,newTableInvertSubPermu);
		
		return subInstance;
	}
	
	public void setAsSubInstanceOf (Instance inst,int[] tableSubPermuX,int[] tableInvertSubPermuX){
		isSubInstance = true;
		parentInstance = inst;
		tableSubPermu = tableSubPermuX;
		tableInvertSubPermu = tableInvertSubPermuX;
		
	}
	
}
