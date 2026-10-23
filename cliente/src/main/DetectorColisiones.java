package main;

import entidad.Entidad;
import entidad.Jugador;

public class DetectorColisiones {
  GamePanel gp;

  public DetectorColisiones(GamePanel gp) {
    this.gp = gp;
  }

  public void revisaTile(Entidad entidad){
    if(entidad instanceof Jugador jugador){
      int izquierdaEntidadMundoX = jugador.getMundoX() + jugador.getAreaSolidaX();
      int derechaEntidadMundoX = jugador.getMundoX() + jugador.getAreaSolidaX() + jugador.getAreaSolidaAncho() ;
      int arribaEntidadMundoY = jugador.getMundoY() + jugador.getAreaSolidaY();
      int abajoEntidadMundoY = jugador.getMundoY() + jugador.getAreaSolidaY() + jugador.getAreaSolidaAlto();
      int colIzquierdaEntidad = izquierdaEntidadMundoX / this.gp.getTileSize();
      int colDerechaEntidad = derechaEntidadMundoX / this.gp.getTileSize();
      int renArribaEntidad = arribaEntidadMundoY / this.gp.getTileSize();
      int renAbajoEntidad = abajoEntidadMundoY / this.gp.getTileSize();
      int numTile1, numTile2;
      switch (jugador.getDireccion()) {
        case "arriba":
          renArribaEntidad = (arribaEntidadMundoY - jugador.getVelocidad()) / gp.getTileSize();
          numTile1 = gp.mTi.getCodigoMapaTiles(renArribaEntidad, colIzquierdaEntidad);
          numTile2 = gp.mTi.getCodigoMapaTiles(renArribaEntidad, colDerechaEntidad);
          if (gp.mTi.getColisionDeTile(numTile1) || gp.mTi.getColisionDeTile(numTile2)) {
            jugador.setColisionActivada(true);
          }
          break;
        case "abajo":
          renAbajoEntidad = (abajoEntidadMundoY + jugador.getVelocidad()) / gp.getTileSize();
          numTile1 = gp.mTi.getCodigoMapaTiles(renAbajoEntidad, colIzquierdaEntidad);
          numTile2 = gp.mTi.getCodigoMapaTiles(renAbajoEntidad, colDerechaEntidad);
          if (gp.mTi.getColisionDeTile(numTile1) || gp.mTi.getColisionDeTile(numTile2)) {
            jugador.setColisionActivada(true);
          }
          break;
        case "izquierda":
          colIzquierdaEntidad = (izquierdaEntidadMundoX - jugador.getVelocidad()) / gp.getTileSize();
          numTile1 = gp.mTi.getCodigoMapaTiles(renArribaEntidad, colIzquierdaEntidad);
          numTile2 = gp.mTi.getCodigoMapaTiles(renAbajoEntidad, colIzquierdaEntidad);
          if (gp.mTi.getColisionDeTile(numTile1) || gp.mTi.getColisionDeTile(numTile2)) {
            jugador.setColisionActivada(true);
          }
          break;
        case "derecha":
          colDerechaEntidad = (derechaEntidadMundoX + jugador.getVelocidad()) / gp.getTileSize();
          numTile1 = gp.mTi.getCodigoMapaTiles(renArribaEntidad, colDerechaEntidad);
          numTile2 = gp.mTi.getCodigoMapaTiles(renAbajoEntidad, colDerechaEntidad);
          if (gp.mTi.getColisionDeTile(numTile1) || gp.mTi.getColisionDeTile(numTile2)) {
            jugador.setColisionActivada(true);
          }
          break;
        default:
          break;
      }
    }
  }
}
