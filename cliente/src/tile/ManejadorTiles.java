package tile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import main.GamePanel;


public class ManejadorTiles {
    private GamePanel gp;
    private int maxTiles = 10;
    Tile[] arregloTiles;
    private int mapCodeTile[][];

    public ManejadorTiles(GamePanel gp){
        this.gp = gp;
        this.arregloTiles = new Tile[maxTiles];
        this.mapCodeTile = new int[gp.getMaxRows()][gp.getMaxCols()];
        getImagenesTile();
        cargarEscenario();
    }
    
    void getImagenesTile(){
        try{
            arregloTiles[0] = new Tile();
            arregloTiles[0].setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/agua.png")));
            arregloTiles[0].setColision(false);

            arregloTiles[1] = new Tile();
            arregloTiles[1].setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/arbol.png")));
            arregloTiles[1].setColision(true);

            arregloTiles[2] = new Tile();
            arregloTiles[2].setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/arena.png")));
            arregloTiles[2].setColision(false);

            arregloTiles[3] = new Tile();
            arregloTiles[3].setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/muro.png")));
            arregloTiles[3].setColision(true);

            arregloTiles[4] = new Tile();
            arregloTiles[4].setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/pasto.png")));
            arregloTiles[4].setColision(false);

            arregloTiles[5] = new Tile();
            arregloTiles[5].setImage(ImageIO.read(getClass().getResourceAsStream("/tiles/suelo.png")));
            arregloTiles[5].setColision(false);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void cargarEscenario() {
        try (InputStream scenario = getClass().getResourceAsStream("/escenarios/mundo.txt")) {
            if (scenario == null) {
                System.err.println("No se encontró /escenarios/map.txt");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(scenario));
            for (int ren = 0; ren < gp.getMaxRows(); ren++) {
                String dataRow = br.readLine();
                if (dataRow == null) break;
                String[] codes = dataRow.trim().split(" ");
                for (int col = 0; col < gp.getMaxCols(); col++) {
                    this.mapCodeTile[ren][col] = Integer.parseInt(codes[col]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2) {
        int size = gp.getTileSize();
        for (int ren = 0; ren < gp.getMaxRows(); ren++) {
            for (int col = 0; col < gp.getMaxCols(); col++) {
                int tile = this.mapCodeTile[ren][col];
                g2.drawImage(arregloTiles[tile].getImage(), size * col, size * ren, size, size, null);
            }
        }
    }

    public int getCodigoMapaTiles(int ren, int col){
        if (ren < 0 || col < 0 || ren >= mapCodeTile.length || col >= mapCodeTile[0].length) {
            return 1; // trátalo como sólido (arbol)
        }
        return this.mapCodeTile[ren][col];
    }

    public boolean getColisionDeTile(int index){
        return this.arregloTiles[index].getColision();
    }
}
