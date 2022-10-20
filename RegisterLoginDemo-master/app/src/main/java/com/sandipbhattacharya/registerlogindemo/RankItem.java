package com.sandipbhattacharya.registerlogindemo;

public class RankItem {

        private String Rank_name;
        private String Rank_score;
        private String Rank_updatetime;

        public RankItem(String rank_name, String score, String updatetime) {
            this.Rank_name = rank_name;
            this.Rank_score = score;
            this.Rank_updatetime = updatetime;
        }

        public String getRank_name() {
            return Rank_name;
        }

        public String getRank_score() {
            return Rank_score;
        }

        public String getRank_updatetime() {
        return Rank_updatetime;
    }

}
