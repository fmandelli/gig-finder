package findagig.batch.job;

import findagig.source.driver.SongKickDriver;
import findagig.source.entity.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Service
public class EventReader implements ItemReader<Event> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<Event> events = new ArrayList<>();
    private SongKickDriver driver;
    private Map<String, Integer> metroAreas = new HashMap<>();
    private int page = 0;

    public EventReader() {
        this.driver = new SongKickDriver();

        //At this point, only events from the below cities will be collected.
        //The HashMap below contains a key/value of City and MetroAreaId
        this.metroAreas.put("WINNIPEG", 27403);
        //this.metroAreas.put("TORONTO", 27396);
        //this.metroAreas.put("VANCOUVER", 27398);
        //this.metroAreas.put("MUNICH", 28549);
    }

    @Override
    public Event read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (this.events.isEmpty()) {
            this.page++;
            //if (page > 1) // no loop. returns only the first page
            //    return null;
            readEvents(page);
        }
        if (!this.events.isEmpty()) {
            return this.events.remove(0);
        }
        return null;
    }

    private void readEvents(int pageNumber) {
        this.metroAreas.forEach((key, value) -> {
            long time = System.currentTimeMillis();
            List<Event> newList = driver.getUpcomingEventsByMetroAreaId(value, pageNumber);
            logger.info("Events read",
                    keyValue("event", "EVENT_READ"),
                    keyValue("METRO_AREA", key),
                    keyValue("PAGE_NUMBER", pageNumber),
                    keyValue("duration", System.currentTimeMillis() - time));
            if (newList != null)
                this.events.addAll(newList);
        });
    }

}
