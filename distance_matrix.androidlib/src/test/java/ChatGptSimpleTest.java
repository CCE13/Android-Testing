import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import static io.grpc.okhttp.internal.Platform.logger;

import android.content.Context;
import android.content.res.AssetManager;

import com.abk.distance.chatgptscript.ChatGptAdvance;
import com.abk.distance.chatgptscript.JsonStreamReader;
import com.abk.distance.chatgptscript.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


class ChatGptSimpleTest {
    @Test
    public void testIfCustomGPTworks(){
        ChatGptAdvance chatGptFunction = new ChatGptAdvance();
        logger.info("Service value: {} " + chatGptFunction.responseMessage);
        assertNotNull(chatGptFunction.responseMessage);

    }


    @Test
    public void testIfEventCanRetrieveDataFromAIServiceBridge(){

        //Remove activity requirement in aiServiceBridge to test
        //AiServiceBridge aiServiceBridge = new AiServiceBridge();
        MessageEvent messageEvent = new MessageEvent("Test test 123");
        EventBus.getDefault().post(messageEvent);

        //logger.info("Event Data Value: " + aiServiceBridge.data );

        //assertNotNull(aiServiceBridge.data);

    }

    @Test
    public void testIfJsonCanReturnValue() throws IOException {

        Context mockContext = Mockito.mock(Context.class);
        AssetManager mockAssetManager = Mockito.mock(AssetManager.class);
        when(mockContext.getAssets()).thenReturn(mockAssetManager);

        JsonStreamReader jsonStreamReader = new JsonStreamReader(mockContext);
        String jsonContent = """
        { 
            "Cadence": 132,
            "HeartRate": 0,
            "PaceMins": 6,
            "PaceSeconds": 26,
            "CurrentPaceMins": 9,
            "CurrentPaceSeconds": 17,
            "FastestPaceMins": 6,
            "FastestPaceSeconds": 30,
            "SlowestPaceMins": 47,
            "SlowestPaceSeconds": 4,
            "BestLapMins": 0,
            "BestLapSeconds": 1,
            "DistanceToTravel": 6.0,
            "DistanceTravelled": 3.0199999809265138,
            "PreciseDistance": 3.01509690284729,
            "DistanceDifferenceFromTheFirstPos": 0.05999999865889549,
            "LapDistance": [
                1.0,
                1.0,
                1.0,
                0.019999999552965165
            ],
            "LapTimings": [
                382.0,
                410.0,
                367.0,
                8.0
            ],
            "TimeTaken": 1167.0,
            "Position": 1,
            "CaloriesBurnt": 299,
            "DayStarted": "Thu",
            "DateStarted": "28 Sep 2023",
            "TimeStarted": "07:48 pm",
            "Mode": "OfflineCompetition"
        }
        """;

        InputStream inputStream = new ByteArrayInputStream(jsonContent.getBytes());
        when(mockAssetManager.open("run.json")).thenReturn(inputStream);




        logger.info("value: " + jsonStreamReader.runData.LapTimings.size());
    }


}