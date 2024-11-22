
package projeto3;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

public class Projeto3 {
    // Tamanho da população e geracoes de familias
    private static final int TAMANHO_POPULACAO = 30,NUM_BITS = 22,NUM_GERACOES = 1000;

    //Intervalo da função
    private static final double INTERVALO_MIN = -10.0,INTERVALO_MAX = 10.0;
    
    // Taxa de crossover e mutacao
    private static final double TAXA_CROSSOVER = 0.75,TAXA_MUTACAO = 0.01;

    // Função que queremos maximizar
    private static double funcao(double x) {
        return -Math.pow(x,2);
    }

    // Avalia a aptidão de um indivíduo
    private static double buscarAptidao(int[] individuo) {
        double x = binarioParaDecimal(individuo);
        return funcao(x);
    }

    // Converte um número binário para decimal
    private static double binarioParaDecimal(int[] binario) {
        double valor = 0;
        for (int i = 0; i < binario.length; i++) {
            valor = valor + (binario[i] * Math.pow(2, binario.length - 1 - i));
        }
        return INTERVALO_MIN + (valor / (Math.pow(2, binario.length) - 1)) * (INTERVALO_MAX - INTERVALO_MIN);
    }

    // Gera uma população inicial de indivíduos
    private static int[][] iniciarPopulacao() {
        Random random = new Random();
        int[][] populacao = new int[TAMANHO_POPULACAO][NUM_BITS];
        for (int i = 0; i < TAMANHO_POPULACAO; i++) {
            for (int j = 0; j < NUM_BITS; j++) {
                populacao[i][j] = random.nextInt(2);
            }
        }
        return populacao;
    }

   // Seleciona um indivíduo usando o método da Roleta
    private static int[] girarRoleta(int[][] populacao, double[] aptidoes) {
         
        Random random = new Random();
        double somaAptidoes = Arrays.stream(aptidoes).sum(); //soma todos os valores de aptidoes daquela populacao
        double pontoSelecao = random.nextDouble() * somaAptidoes; //variavel de controle
        double acumulado = 0;

        for (int i = 0; i < populacao.length; i++) {
            acumulado = acumulado + aptidoes[i]; //o objetivo é somar até chegar na variavel de controle, quando chegar retorna o individuo, por isso girarRoleta
            if (acumulado >= pontoSelecao) {
                return populacao[i].clone();
            }
        }
        return populacao[populacao.length - 1].clone();
    }

    // Realiza o crossover de dois pontos entre dois indivíduos
    private static int[][] crossover(int[] pai1, int[] pai2) {
        Random random = new Random();
        int pontoCorte1 = random.nextInt(NUM_BITS); //19
        int pontoCorte2 = random.nextInt(NUM_BITS - pontoCorte1) + pontoCorte1; //21
        
        int[][] filhos = new int[2][NUM_BITS]; //Todo Crossover gera 2 filhos, os 2 pais com bits invertidos em lugares especificos
        
        for (int i = 0; i < NUM_BITS; i++) {
            if (i>= pontoCorte1 || i <= pontoCorte2) {  //mistura
                filhos[0][i] = pai2[i];
                filhos[1][i] = pai1[i];
            } else {    //nao mistura
                filhos[0][i] = pai1[i];
                filhos[1][i] = pai2[i];
            }
        }
        return filhos;
    }

    // Realiza a mutação de um bit
    private static void mutacao(int[] individuo) {
        Random random = new Random();
        for (int i = 0; i < individuo.length; i++) {
            if (random.nextDouble() < TAXA_MUTACAO) {//Se for menor que 0.001 faça
                individuo[i] = 1 - individuo[i]; // Inverte o bit, se for 1-1=0 se for 1-0=1
            }
        }
    }

    // Encontra o melhor indivíduo em uma população
    private static int[] encontrarMelhorIndividuo(int[][] populacao) {
        //inicia com o primeiro da populacao, analise em lista
        int[] melhorIndividuo = populacao[0];
        double melhorAptidao = buscarAptidao(melhorIndividuo);
        
        for (int i = 1; i < populacao.length; i++) {
            double aptidaoAtual = buscarAptidao(populacao[i]);
            if (aptidaoAtual > melhorAptidao) {
                melhorIndividuo = populacao[i];
                melhorAptidao = aptidaoAtual;
            }
        }
        return melhorIndividuo;
    }

 public static void main(String[] args) {
        Random random = new Random();
        var best = Double.MIN_VALUE;
        DecimalFormat df = new DecimalFormat("0.0000000");
        int[][] populacao = iniciarPopulacao(); //somente inicia a populacao
        
        for (int geracao = 0; geracao < NUM_GERACOES; geracao++){   //Quantidade de geração para achar o melhor
            double[] aptidoes = new double[TAMANHO_POPULACAO];
            
            for (int i = 0; i < TAMANHO_POPULACAO; i++) {
                aptidoes[i] = buscarAptidao(populacao[i]);
                if(aptidoes[i]>best){
                    best=aptidoes[i];
                }
            } //salva a aptidoes de cada um daquela geração

            
            int[][] novaPopulacao = new int[TAMANHO_POPULACAO][NUM_BITS]; //nova geracao alterada a partir da passada
            
            for (int i = 0; i < TAMANHO_POPULACAO; i += 2){ //2 em 2, pois são 2 pais de 1 vez
                //Individuos Escolhido pela roleta
                int[] pai1 = girarRoleta(populacao, aptidoes);
                int[] pai2 = girarRoleta(populacao, aptidoes);
                
                
                //Chance de acontecer a mistura entre os pais 0.0 a 0.6
                //Chance de não acontecer mistura entre os pais 0.7 a 1 
                if (random.nextDouble() < TAXA_CROSSOVER) { 
                    int[][] filhos = crossover(pai1, pai2); //recebe os 2 pais modificados ou seja filhos
                    //chance de mutacao, alterar dna novamente
                    mutacao(filhos[0]); mutacao(filhos[1]);
                    //salva na nova geracao
                    novaPopulacao[i] = filhos[0]; novaPopulacao[i + 1] = filhos[1];
                }else {
                    //salva na nova geracao
                    novaPopulacao[i] = pai1; novaPopulacao[i + 1] = pai2;
                }
            }

            populacao = novaPopulacao; //altera a geracao pela nova dos filhos

            if (geracao == NUM_GERACOES - 1) { //Ultima geracao foi analisada
                int[] melhorIndividuo = encontrarMelhorIndividuo(populacao);
                double melhorAptidao = buscarAptidao(melhorIndividuo);
                if(melhorAptidao>best){
                    best=melhorAptidao;
                }
                
                System.out.println("Melhor: " + Arrays.toString(melhorIndividuo) + "\nAptidao: " + df.format(melhorAptidao) + "\n10 Melhores:");
                
                
                Double[] aptidoes10 = new Double[aptidoes.length];
                for (int i = 0; i < aptidoes.length; i++) {
                    aptidoes10[i] = aptidoes[i];
                }
                
                Arrays.sort(aptidoes10,Comparator.reverseOrder());
                for (int i = 0; i < 10; i++) {
                    if (i==0) {
                        System.out.println("Melhor individuo - " + df.format(aptidoes10[i]));
                    }else{
                    System.out.println((i+1) + "- " + df.format(aptidoes10[i]));
                    }
                }
                System.out.printf("best- " + best);
            }
        }
    }
}

