package org.example.netty.common.dto;

public class RegRequest implements BasicRequest {


        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        private String login;
        private String password;
        private String nick;
        public RegRequest(String login, String password) {
            this.login = login;
            this.password = password;
            this.nick = login;

        }
        @Override
        public String getType() {
            return "RegRequest";
        }

}
