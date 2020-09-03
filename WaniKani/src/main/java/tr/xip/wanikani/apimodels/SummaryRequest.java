package tr.xip.wanikani.apimodels;

import java.util.List;

public class SummaryRequest {
    public String object;
    public String url;
    public String data_updated_at;
    public SummaryData data;

    public class SummaryData {
        public List<Lesson> lessons;
        public List<Reviews> reviews;
        public String next_reviews_at;

        public class Lesson {
            public String available_at;
            public List<Integer> subject_ids;
        }

        public class Reviews {
            public String available_at;
            public List<Integer> subject_ids;
        }
    }
}
