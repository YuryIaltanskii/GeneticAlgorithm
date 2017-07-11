import java.util.ArrayList;
import java.util.Random;
import Base.*;


public class Main 
{
	private static SimulationData _simulationData; //Contiene informacion para el algoritmo genetico
	private static ArrayList<Chromosome> _population; //Poblacion
	private static int _currentGeneration = 0;	//Lleva la cuenta de las generaciones.
	private static ArrayList<Chromosome> _selectedParents; //Lista de individuos seleccionados para ser padres.
	private static ArrayList<Chromosome> _childrens;	//Lista de individuos creados en la operacion de 'crossover'
	
	public static void main(String[] args) 
	{
		_simulationData = new SimulationData();
		
		//poblacion inicial 150
		createFirstGeneration(150);
		
		System.out.println(">>> Comienza la simulacion!!!");
		
		while ( !solutionFounded() )
		{
			showCurrentPopulationOnConsole();
			_currentGeneration++;
			
			if(_currentGeneration > 1000000)
			{
				System.out.println(">>> ERROR: Pasaron 1.000.000 de generaciones y no se encontro la solucion...");
				break;
			}
			
			selection(); 
			crossover();
			mutation();
			updatePopulation();
		}
		
		System.out.println("############### END  #############################");
		}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	
	public static void selection()
	{
		_population = _simulationData.sortItemsByFitness(_population);
		
		_selectedParents = _simulationData.getItemsByRoulletteWheelSelection(_population, 50);
	}

	public static void crossover()
	{
		if(_childrens == null)
			_childrens = new ArrayList<Chromosome> ();
		else
			_childrens.clear();
		
		for (int i = 0; i < 25; i++) {
			Chromosome padre1 = _selectedParents.get(i * 2);
			Chromosome padre2 = _selectedParents.get(i * 2 + 1);
			Random rnd = new Random();
			int indx = rnd.nextInt(padre1.genes.size());
			Chromosome hijo1 = new Chromosome();
			Chromosome hijo2 = new Chromosome();
			for (int j = 0; j < indx; j++) {
				hijo1.genes.add(padre1.genes.get(j));
				hijo2.genes.add(padre2.genes.get(j));
			}
			for (int k = indx; k < padre1.genes.size(); k++) {
				hijo1.genes.add(padre2.genes.get(k));
				hijo2.genes.add(padre1.genes.get(k));
			}
			_childrens.add(hijo1);
			_childrens.add(hijo2);
		}
	}
	
	public static void mutation()
	{
		Random rnd = new Random();
		
		float mutationProbability = 2; 
		for (Chromosome chromosome : _childrens) 
		{
			if(rnd.nextInt(100) <= mutationProbability)
			{
				int indice1 = rnd.nextInt(chromosome.genes.size()-1);
				int indice2 = indice1 + 1 + rnd.nextInt(chromosome.genes.size()-indice1-1);
				int tmp = chromosome.genes.get(indice1);
				chromosome.genes.set(indice1, chromosome.genes.get(indice2));
				chromosome.genes.set(indice2, tmp);
			}
		}
	}
	
	public static void updatePopulation()
	{
		_population.clear();
		_population.addAll(_selectedParents);
		_population.addAll(_childrens);
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////// 
	
	/**
	   * Crea la generacion base para el algoritmo.
	   * Solo debe llamarse al comenzar la simulacion o
	   * tras forzar su reinicio.
	   * @param populationSize Cantidad de individuos en la poblacion
	   */
	private static void createFirstGeneration(int populationSize)
	{
		_population = new ArrayList<Chromosome>();
		
		int amountOfCities = _simulationData.getAmountOfCities();
		Random rnd = new Random();
		
		for (int i = 0; i < populationSize; i++) 
		{
			//Inicializamos al individuo
			Chromosome chromosome = new Chromosome();
			
			//Seteamos randomicamente los valores de cada gen.
			for (int j = 0; j < amountOfCities; j++) 
			{
				//Obtenemos un valor aleatorio
				int possibleGen = rnd.nextInt(amountOfCities);
				
				//Si el cromosoma ya contiene este valor, volvemos a obtener un valor aleatorio
				while (chromosome.genes.contains(possibleGen)) 
				{
					possibleGen = rnd.nextInt(amountOfCities);
				}
				
				//Una vez que nos garantizamos que este valor no esta en el cromosoma, lo agregamos.
				chromosome.genes.add(possibleGen);
			}
			
			//Agregamos al individuo a la poblacion.
			_population.add(chromosome);
		}
	}
	
	/**
	 * Chequea si dentro de la generacion actual se encuentra la mejor solucion
	 * @return 'True' si la solucion fue encontrada
	 */
	private static boolean solutionFounded()
	{
		boolean solutionFounded = false;
		int solutionIndex = -1;
		
		for (int i = 0; i < _population.size(); i++) 
		{			
			//Si el fitness de este individuo es mejor o igual a la solucion 'ideal'
			//Guardamos el indice de la solucion.
			if(_simulationData.calculateFitness(_population.get(i)) >= _simulationData.bestApproximateFitness())
			{
				//Si ya encontramos un individuo con buen fitness lo comparamos con el actual
				if(solutionFounded)
				{
					//Si es mejor, lo reemplazamos
					if(_simulationData.calculateFitness(_population.get(i)) >= _simulationData.calculateFitness(_population.get(solutionIndex)))
					{
						solutionFounded = true;
						solutionIndex = i;	
					}
				}
				else
				{
					solutionFounded = true;
					solutionIndex = i;
				}
			}
		}
		
		//Si encontramos la solucion, lo mostramos por consola!
		if(solutionFounded)
		{
			System.out.println("~~~ SOLUTION FOUND!!! ~~~");
			System.out.println("GENERATION: " + _currentGeneration);
			System.out.println("CHROMOSOME: ");
			
			for (int i = 0; i < _population.get(solutionIndex).genes.size(); i++) 
			{
				System.out.println("--->>> " + _population.get(solutionIndex).genes.get(i));	
			}
			
			System.out.println("--->>> DISTANCE: " + _simulationData.calculateDistanceBetweenCities(_population.get(solutionIndex)));
			System.out.println("--->>> FITNESS: " + String.format("%.10f", _simulationData.calculateFitness(_population.get(solutionIndex))));
		}
		
		return solutionFounded;
	}
	
	
	//Simplemente a modo informativo. No hace falta implementarlo
	private static void showCurrentPopulationOnConsole()
	{
		System.out.println("~~~ GENERATION " + _currentGeneration + " ~~~");
		
		
		for (int i = 0; i < _population.size(); i++) 
		{
			System.out.println("CITIZEN: " + i);
			for (int j = 0; j < _population.get(i).genes.size(); j++) 
			{
				System.out.println("--->>> " + _population.get(i).genes.get(j));	
			}
			
			System.out.println("--->>> DISTANCE: " + _simulationData.calculateDistanceBetweenCities(_population.get(i)));
			System.out.println("--->>> FITNESS: " + _simulationData.calculateFitness(_population.get(i)));
		}
		
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
	}

}
