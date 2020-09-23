package com.mrothberg.kakumei.apimodels;

import java.util.List;

public class Subjects {

    public int total_count;
    public List<SubjectItem> data;

    public class SubjectItem {
        public int id;
        public String object;
        public SubjectItemData data;

        public class SubjectItemData {
            public int level;
            public String characters;
            public String slug;
            public List<Readings> readings;
            public List<Meanings> meanings;
            public List<CharacterImages> character_images;

            public class Readings {
                public String type;
                public String reading;
                public boolean primary;
            }

            public class Meanings {
                public String meaning;
                public boolean primary;
            }

            public class CharacterImages {
                public String url;
                public Metadata metadata;
                public String content_type;

                public class Metadata {
                    public String color;
                    public String dimensions;
                    public String style_name;
                }
            }
        }
    }
}
