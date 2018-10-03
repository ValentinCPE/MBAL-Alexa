import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class IsLetterHandler implements com.amazon.ask.dispatcher.request.handler.RequestHandler {

    private String token;

    @Override
    public boolean canHandle(HandlerInput handlerInput) {

        boolean canFollow = false;

        token = handlerInput.getRequestEnvelope()
                    .getContext()
                    .getSystem()
                    .getUser()
                    .getAccessToken();

        if(token != null && !token.isEmpty()){
            canFollow = true;
        }

       /* try {
            URL url = new URL("https://www.mbal.ovh/MBAL/oauth/token?grant_type=password&username=admin&password=Valentin34");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Basic bXktdHJ1c3RlZC1jbGllbnQ6c2VjcmV0");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int status = con.getResponseCode();

            if(status == 200){
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(content.toString());

                token = json.getString("access_token");
                canFollow = true;
            }

            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } */



        return canFollow;
    }

    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {

        String response = "Non trouvé";

        try {
            URL url = new URL("https://www.mbal.ovh/MBAL/api/amazon/getEventAtDateAmazon");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer "+token);
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);

            int status = con.getResponseCode();

            if(status == 200){
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject json = new JSONObject(content.toString());

                LocalDateTime localDateTime = new Timestamp(json.getLong("timestamp")).toLocalDateTime();
                DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
                DateTimeFormatter formatterHour = DateTimeFormatter.ofPattern("HH");
                DateTimeFormatter formatterMinute = DateTimeFormatter.ofPattern("mm");

                switch(json.getString("message")){

                    case "NEW_COURRIEL":
                        response = "Vous avez du nouveau courrier depuis le "+localDateTime.format(formatterDate)+" à "+localDateTime.format(formatterHour)+" heures "+localDateTime.format(formatterMinute);
                        break;

                    case "COURRIEL_FETCHED":
                        response = "Le dernier courrier a été récupéré à "+localDateTime.format(formatterHour)+" heures "+localDateTime.format(formatterMinute)+" le "+localDateTime.format(formatterDate)+" par "+
                                json.getJSONObject("user").getString("prenom")+" "+json.getJSONObject("user").getString("nom");
                        break;

                    default:
                        response = "Votre famille " + json.getJSONObject("family").getString("name") + " n'a jamais reçu d'évènements !";
                        break;

                }

            }else{
                response = "Vous n'êtes pas liés à une famille ! Accédez à l'application Android èm bé à èl afin de faire ce lien !";
            }

            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return handlerInput.getResponseBuilder().withSpeech(response).build();
    }

}
