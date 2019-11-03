# FishingGame
Android 1:1 game app. Network works by TCP/IP. Graphic work by Open GL ES 2.0

Screenshots
---------------------------------------------------------------------------------------------
![K-014](https://user-images.githubusercontent.com/54348567/68066489-3ea0ac00-fd7c-11e9-8caf-0e7e6c1a59c1.png)
![K-015](https://user-images.githubusercontent.com/54348567/68066490-3f394280-fd7c-11e9-89af-653d576e3785.png)
![K-016](https://user-images.githubusercontent.com/54348567/68066491-3f394280-fd7c-11e9-895f-05e27bd06e26.png)
![K-017](https://user-images.githubusercontent.com/54348567/68066492-3f394280-fd7c-11e9-9794-f40cb355f9fa.png)
![K-018](https://user-images.githubusercontent.com/54348567/68066493-3f394280-fd7c-11e9-8b27-59a686f6deff.png)
<br>

App Play Video Link
--------------------------------
https://www.youtube.com/watch?v=03uGE45YfM0
<br>

App Info
-------------------------------------------------------------------
채팅 기능이 들어간 1:1 네트워크 게임 <br>
일정시간 마다 나오는 몬스터를 작살을 던져 죽이는 방식 <br>
등장하는 몬스터를 전부 다 처치하면 게임 종료 <br>
<br>


기능
- 회원가입, 로그인(페이스북 및 카카오 연동)
- tcp 소켓 통신을 이용한 실시간 채팅(대기실 채팅, 개인채팅, 귓속말)
- OpenGL ES 2.0을 이용한 모든 게임 화면 렌더링
- 게임 내 데이터(자기 작살의 위치, 획득한 점수 등등)및 게임진행을 서버에서 관리
- 게임중 유저간 채팅가능
- 로그인 로그와 게임로그를 MongoDB에 저장
<br>


기술 스택
- OS: Linux(CentOS), Android <br>
- Language: Java, PHP <br>
- Web Server: Nginx <br>
- Database: Postgresql, MongoDB <br>
- Library: OpenGL ES2.0 <br>
- Protocol: HTTP, TCP/IP <br>
