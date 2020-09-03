package tr.xip.wanikani.apimodels;

import java.util.List;

public class Assignments {

    public int total_count;
    public Pages pages;
    public List<Assignment> data;

    public class Assignment {
        public int id;
        public String object;
        public AssignmentData data;

        public class AssignmentData {
            public String subject_type;
            public int srs_stage;
            public String passed_at;
            public int subject_id;
            public String burned_at;
            public String unlocked_at;
            public String available_at;
        }
    }

    public class Pages {
        public String per_page;
        public String next_url;
        public String previous_url;
    }
}
