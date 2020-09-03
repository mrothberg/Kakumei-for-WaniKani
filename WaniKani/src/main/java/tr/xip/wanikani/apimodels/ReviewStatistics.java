package tr.xip.wanikani.apimodels;

import java.util.List;

public class ReviewStatistics {
    public int total_count;
    public String data_updated_at;
    public List<ReviewStatistic> data;

    public class ReviewStatistic {
        public int id;
        public ReviewStatisticData data;

        public class ReviewStatisticData {
            public int subject_id;
            public String subject_type;
            public int meaning_correct;
            public int meaning_incorrect;
            public int meaning_max_streak;
            public int meaning_current_streak;
            public int reading_correct;
            public int reading_incorrect;
            public int reading_max_streak;
            public int reading_current_streak;
            public int percentage_correct;
            public boolean hidden;
        }
    }
}
