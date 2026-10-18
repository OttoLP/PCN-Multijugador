package red;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;

public final class ConversorJSON {
    private static final Gson GSON = new GsonBuilder()
            .setStrictness(Strictness.STRICT).create();
    private ConversorJSON() {}

    public static String aJson(Mensaje mensaje) {
        return GSON.toJson(mensaje);
    }
    
    public static Mensaje desdeJson(String json) {
        Mensaje mensaje = GSON.fromJson(json, Mensaje.class);
        if (mensaje == null || mensaje.tipo == null) {
            throw new IllegalArgumentException("Falta el tipo de mensaje");
        }
        return mensaje;
    }
}
