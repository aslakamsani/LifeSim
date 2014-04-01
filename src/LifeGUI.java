import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;

//GUI to draw and show simulation
public class LifeGUI extends MouseAdapter implements ActionListener{
    DrawingPanel drawingPanel; //panel to draw on
    JRadioButton step, cont; //determine speed of simulation
    JToggleButton start; //start simulation
    JLabel genLabel; //generations passed to show
    //JSlider speed; //speed up continuous simulation
    Color currentColor = Color.black;
    char[][] display = new char[20][20]; //cells to show
    LifeCalc life;
    javax.swing.Timer timer; //control generation display speed
    boolean sameRun = false;
    //keeps track of cells, always 20x20

    LifeGUI(){
    	//create window
        JFrame window = new JFrame("Life GUI");
        window.setBounds(100,100,445,600);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //initialize label
        genLabel = new JLabel("Generation 0");
        genLabel.setBounds(185,5,125,10);

        //create actual display
        drawingPanel = new DrawingPanel();
        drawingPanel.setBounds(20,20,400,400);
        drawingPanel.setBorder(BorderFactory.createEtchedBorder());
        drawingPanel.addMouseListener(this);
        drawingPanel.addMouseMotionListener(this);

        //create buttons
        JButton reset = new JButton("Reset");
        reset.setBounds(115,520,75,20);
        reset.addActionListener(this);

        JButton color = new JButton("Change Color");
        color.setBounds(215,520,125,20);
        color.addActionListener(this);

        start = new JToggleButton("Start");
        start.addActionListener(this);

        step = new JRadioButton("Step",true);
        cont = new JRadioButton("Continuous");
        //speed = new JSlider(1,30);

        //put buttons together
        ButtonGroup speed = new ButtonGroup();
        speed.add(step);
        speed.add(cont);
        JPanel speeds = new JPanel();
        speeds.add(step);
        speeds.add(cont);
        
        //put panel together
        JPanel lifePanel = new JPanel();
        lifePanel.setBorder(BorderFactory.createTitledBorder("Life Simulator"));
        lifePanel.setBounds(75,425,300,85);
        lifePanel.setLayout(new GridLayout(2,1));
        lifePanel.add(speeds);
        lifePanel.add(start);
        
        //put everything together
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.add(genLabel);
        mainPanel.add(drawingPanel);
        mainPanel.add(lifePanel);
        mainPanel.add(color);
        mainPanel.add(reset);

        //create menus
        JMenuBar menuBar = new JMenuBar();
        JMenu file = new JMenu("File");
        file.setMnemonic('F');
        JMenu edit = new JMenu("Edit");
        edit.setMnemonic('E');
        JMenuItem open = new JMenuItem("Open",'O');
        JMenuItem save = new JMenuItem("Save",'S');
        open.addActionListener(this);
        save.addActionListener(this);
        file.add(open); file.add(save);
        JMenuItem menuColor = new JMenuItem("Change Color",'C');
        JMenuItem clear = new JMenuItem("Clear",'L');
        menuColor.addActionListener(this);
        clear.addActionListener(this);
        edit.add(menuColor); edit.add(clear);
        menuBar.add(file); menuBar.add(edit);

        window.setJMenuBar(menuBar);
        window.getContentPane().add(mainPanel);
        window.setVisible(true);

        //set colors to all white
        for(int i=0; i<display.length; i++){
            for(int j=0; j<display[i].length; j++)
                display[i][j] = ' ';
        }

        life = new LifeCalc(display);
        timer = new javax.swing.Timer(100,this);
    }
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();
        //start simulation or show next generation
        if(e.getSource()==start||e.getSource()==timer){
            if(start.isSelected()) start.setText("Stop");
            else{
                start.setText("Start");
                if(timer.isRunning()) timer.stop();
            }
            //if simulation is restarted, re-initialize
            if(!sameRun){
                life = new LifeCalc(display);
                sameRun = true;
            }
            
            //step through generations
            if(step.isSelected()&&start.isSelected()){
                stepGen();
                start.setSelected(false);
                start.setText("Start");
            //show generations at 10 fps
            }else if(cont.isSelected()&&start.isSelected()){
                if(!(timer.isRunning())) timer.restart();
                else stepGen();
            }
        }
        
        //clear board and reset
        else if(command.equals("Reset")||command.equals("Clear")){
            drawingPanel.repaint();
            drawingPanel.paintComponent(drawingPanel.getGraphics());
            for(int i=0; i<display.length; i++){
                for(int j=0; j<display[i].length; j++)
                    display[i][j] = ' ';
            }
            sameRun = false;
            genLabel.setText("Generation 0");
            
        //allow user to choose color to draw with
        }else if(command.equals("Change Color")){
            JColorChooser chooser = new JColorChooser();
            currentColor = chooser.showDialog(null,"Change Color",null);
        }

        //open PPM file to initialize board
        else if(command.equals("Open")){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("PPM", "ppm"));
            int option = chooser.showOpenDialog(null);
            if(option==JFileChooser.APPROVE_OPTION){
                File f = chooser.getSelectedFile();
                try{
                    openFile(f);
                }catch(Exception x){
                    System.out.println(x+x.getMessage());
                }
            }
            
        //save PPM file to save board
        }else if(command.equals("Save")){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("PPM", "ppm"));
            int option = chooser.showSaveDialog(null);
            if(option==JFileChooser.APPROVE_OPTION){
                File f = chooser.getSelectedFile();
                try{
                    saveFile(f);
                }catch(Exception x){
                    System.out.println(x+x.getMessage());
                }
            }
        }
    }
    
    //fill cell for left-click, clear otherwise
    public void mouseClicked(MouseEvent e){
        int x = e.getX(); int y = e.getY();
        Graphics g = drawingPanel.getGraphics();
        if(e.isMetaDown()) drawingPanel.clearCell(x,y,g);
        else drawingPanel.fillCell(x,y,g);
        g.dispose();
    }
    
    //fill or clear multiple cells on drag
    public void mouseDragged(MouseEvent e){
        int x = e.getX(); int y = e.getY();
        Graphics g = drawingPanel.getGraphics();        
        if(e.isMetaDown()) drawingPanel.clearCell(x,y,g);
        else drawingPanel.fillCell(x,y,g);
        g.dispose();
    }
    
    //reads PPM file and initializes board with figure
    private void openFile(File f) throws IOException{
        //take in colors line by line from a 20x20 grid
        try{
            Scanner in = new Scanner(f);
            //check header
            if(!(in.nextLine().equals("P3"))) throw new IOException("File format.");
            boolean dimensions = false;
            int x=0; int y=0;
            while(!dimensions){
                String piece = in.nextLine();
                Scanner dim = new Scanner(piece);
                if(piece.charAt(0)=='#');
                else{
                    x = dim.nextInt(); y = dim.nextInt();
                    dimensions = true;
                }
            }
            //reset board
            Graphics dp = drawingPanel.getGraphics();
            drawingPanel.paintComponent(dp);
            for(int i=0; i<display.length; i++){
                for(int j=0; j<display[i].length; j++)
                    display[i][j] = '*';
            }
            //take in image as described in file
            for(int i=0; i<y; i++){
                String piece = in.nextLine();
                Scanner rgb = new Scanner(piece);
                if(piece.charAt(0)=='#'||piece.equals("255")) i--;
                else{
                    for(int j=0; j<x; j++){
                        int r = rgb.nextInt();
                        int g = rgb.nextInt();
                        int b = rgb.nextInt();
                        Color c = new Color(r,g,b);
                        //fill cell with color
                        drawingPanel.setCell(j,i,c,dp);
                    }
                }
            }
            dp.dispose();
        }catch(Exception e){
            System.out.println(e+e.getMessage());
        }
    }
    
    //save display as PPM file
    private void saveFile(File f){
        //write colors by line, on a 20x20 grid
        try{
            FileWriter scribe = new FileWriter(f);
            //write header
            scribe.write("P3\r\n20 20\r\n255\r\n");
            
            //write each cell
            for(int i=0; i<display.length; i++){
                for(int j=0; j<display[i].length; j++){
                    Color c = null;
                    if(display[i][j]=='*') c = currentColor;
                    else c = Color.white;
                    scribe.write(""+c.getRed()+" "+c.getGreen()+" "+c.getBlue());
                    scribe.write("\t");
                }
                scribe.write("\r\n");
            }
            scribe.close();
        }catch(Exception e){
            System.out.println(e+e.getMessage());
        }
    }
    
    //calculate and display next generation
    private void stepGen(){
        Graphics g = drawingPanel.getGraphics();
        char[][] temp = life.calcBoard(1);
        for(int s=0; s<20; s++){
            for(int t=0; t<20; t++){
                display[s][t] = temp[s][t];
                if(temp[s][t]=='*')
                    drawingPanel.setCell(t,s,currentColor,g);
                else drawingPanel.setCell(t,s,Color.white,g);
            }
        }
        genLabel.setText("Generation "+life.getGen());
    }

    private class DrawingPanel extends JPanel{
        //draw grid
    	public void paintComponent(Graphics g){
            g.setColor(Color.white);
            g.fillRect(2,2,this.getWidth()-2,this.getHeight()-2);
            g.setColor(Color.lightGray);
            for(int x=0; x<this.getWidth(); x+=20)
                g.drawLine(x,0,x,this.getHeight());
            for(int y=0; y<this.getHeight(); y+=20)
                g.drawLine(0,y,this.getWidth(),y);
        }
    	//fill cell in grid
        public void fillCell(int x,int y,Graphics g){               
            int rectX = 0; int rectY = 0;
            for(int i=x; i>=x-20; i--)
                if(i%20 == 0) rectX = i;
            for(int j=y; j>=y-20; j--)
                if(j%20 == 0) rectY = j;

            g.setColor(currentColor);
            g.fillRect(rectX+1,rectY+1,18,18);
            display[rectY/20][rectX/20] = '*';
        }
        //clear cell in grid
        public void clearCell(int x,int y,Graphics g){          
            int rectX = 0; int rectY = 0;
            for(int i=x; i>=x-20; i--)
                if(i%20 == 0) rectX = i;
            for(int j=y; j>=y-20; j--)
                if(j%20 == 0) rectY = j;

            g.setColor(Color.white);
            g.fillRect(rectX+1,rectY+1,18,18);
            display[rectY/20][rectX/20] = ' ';
        }
        //set cell as specified in file
        public void setCell(int x,int y,Color c,Graphics g){
            g.setColor(c);
            g.fillRect(x*20+1,y*20+1,18,18);
            if(c.equals(Color.white)) display[y][x] = ' ';
            else display[y][x] = '*';
        }
    }
}