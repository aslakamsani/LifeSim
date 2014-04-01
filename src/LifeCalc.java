import java.io.*;
import java.util.*;

//calculates next generation according to Conway's rules
public class LifeCalc{
	//hold current and next generations
    private char[][] init, next;
    //generation number
    private int gen;

    //sets up life simulation
    LifeCalc(char[][] input){
        gen = 0;
        createBoard(input);
    }

    //initializes board
    private void createBoard(char[][] input){
        int r = input.length;
        int c = input[0].length;
        init = new char[r][c];
        next = new char[r][c];
        for(int s=0; s<input.length; s++){
            for(int t=0; t<input[s].length; t++){
                init[s][t] = input[s][t];
                next[s][t] = input[s][t];
            }
        }
    }
    
    //calculation determining a cell's life or death
    public char[][] calcBoard(int iter){
        for(int g=1; g<=iter; g++){
            for(int i=0; i<init.length; i++){
                for(int j=0; j<init[i].length; j++){
                    int live = countLive(j,i);
                    //if lonely or overcrowded, cell dies
                    if(live<2 || live>3) next[i][j] = ' ';
                    //cell is maintained
                    else if(live==2) next[i][j] = init[i][j];
                    //with enough neighbors, cell is born
                    else if(live==3) next[i][j] = '*';
                }
            }
            gen++;
            //transfer new generation as current generation
            for(int s=0; s<init.length; s++){
                for(int t=0; t<init[s].length; t++)
                    init[s][t] = next[s][t];
            }
        }
        return next;
    }
    
    //count live neighbors of a cell
    private int countLive(int x,int y){
        int live = 0;
        for(int i=y-1; i<=y+1; i++){
            for(int j=x-1; j<=x+1; j++){
                try{
                    if(i==y && j==x);
                    else if(init[i][j]=='*')live++;
                }catch(Exception e){}
            }
        }
        return live;
    }
    
    public int getGen(){
        return gen;
    }
}