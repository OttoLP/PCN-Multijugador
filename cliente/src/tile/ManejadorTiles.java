package tile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import main.GamePanel;


public class ManejadorTiles {
    private GamePanel gp;
    private static final int TAM_IMAGEN = 16; // los tiles miden 16 x 16 px
    private int maxTiles = 32;
    Tile[] arregloTiles;
    private int mapCodeTile[][];

    public ManejadorTiles(GamePanel gp){
        this.gp = gp;
        this.arregloTiles = new Tile[maxTiles];
        this.mapCodeTile = new int[gp.getWorldRows()][gp.getWorldCols()];
        getImagenesTile();
        cargarEscenario();
    }
    
    // Los tiles se definen en escenarios/tiles.txt (lo lee tambien el servidor).
    void getImagenesTile(){
        try (InputStream definicion = getClass().getResourceAsStream("/escenarios/tiles.txt")) {
            if (definicion == null) {
                System.err.println("No se encontro /escenarios/tiles.txt");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(definicion, StandardCharsets.UTF_8));
            String linea;
            while ((linea = br.readLine()) != null) {
                int comentario = linea.indexOf('#');
                if (comentario >= 0) linea = linea.substring(0, comentario);
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                // codigo  imagen  bloquea  lento  cura
                String[] campos = linea.split("\\s+");
                Tile tile = new Tile();
                tile.setImage(cargarImagen(campos[1]));
                tile.setColision(campos[2].equals("1"));
                tile.setLento(campos[3].equals("1"));
                tile.setCura(campos[4].equals("1"));
                arregloTiles[Integer.parseInt(campos[0])] = tile;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // "archivo.png" o "archivo.png@col,fila" para tomar un tile de 16 x 16 de una hoja de tiles.
    private BufferedImage cargarImagen(String especificacion) throws IOException {
        int arroba = especificacion.indexOf('@');
        String archivo = arroba < 0 ? especificacion : especificacion.substring(0, arroba);
        BufferedImage hoja = ImageIO.read(getClass().getResourceAsStream("/tiles/" + archivo));
        if (arroba < 0) return hoja;
        String[] posicion = especificacion.substring(arroba + 1).split(",");
        int col = Integer.parseInt(posicion[0]);
        int fila = Integer.parseInt(posicion[1]);
        return hoja.getSubimage(col * TAM_IMAGEN, fila * TAM_IMAGEN, TAM_IMAGEN, TAM_IMAGEN);
    }

    public void cargarEscenario() {
        try (InputStream scenario = getClass().getResourceAsStream("/escenarios/mundo.txt")) {
            if (scenario == null) {
                System.err.println("No se encontró /escenarios/mundo.txt");
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(scenario));
            for (int ren = 0; ren < gp.getWorldRows(); ren++) {
                String dataRow = br.readLine();
                if (dataRow == null) break;
                String[] codes = dataRow.trim().split("\\s+");
                for (int col = 0; col < gp.getWorldCols(); col++) {
                    this.mapCodeTile[ren][col] = Integer.parseInt(codes[col]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2, int camaraX, int camaraY) {
        int size = gp.getTileSize();
        int ultimaCol = (camaraX + gp.getWidthScreen() - 1) / size;
        int ultimaRen = (camaraY + gp.getHeightScreen() - 1) / size;
        for (int ren = camaraY / size; ren <= ultimaRen; ren++) {
            for (int col = camaraX / size; col <= ultimaCol; col++) {
                int tile = mapCodeTile[ren][col];
                int pantallaX = size * col - camaraX;
                int pantallaY = size * ren - camaraY;
                g2.drawImage(arregloTiles[tile].getImage(), pantallaX, pantallaY, size, size, null);
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

    public Tile getTile(int index){
        return this.arregloTiles[index];
    }
}
