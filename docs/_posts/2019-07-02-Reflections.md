Welcome to our Reflections blog. Here, we will present the problems that we dealt with and also the lessons that we learned while developing this application as a team. It is always good to reflect at the end of the project. 

# Technical Challenges Our Team Encountered

These are the technical problems we faced during the application development:  

# ![Deadline image]({{site.baseurl}}/images/new-software2.png "Technical Challenges to Encounter")

## Login/Signup Issue

In the start, we were planning on using Google Sign In functionality instead of asking user to Register, but this created an issue when we tried to fetch user’s phone number using Google Sign In since we need to use the user’s phone number as a unique identifier. When we were not able to solve it, we switched back to classic Login/Register flow.

## Google Maps Issue

We planned on using Google Maps API as a back-end service, but this proved to be a network bottleneck. We had to request our backend API with certain input parameters and then backend API will request Google Maps API to fetch the corresponding journey details and then it will send back these details to Android. Here we were making two requests in a single HTTP cycle and that is network expensive. It also created a problem when a customer asks us to have a functionality of autocomplete suggestions for destination. We finally decided to call google Maps API directly from Android and do computations on the data directly at the application side. 

## Fetching Device Contacts Issue

We found ourselves stuck when it came around to get user’s contacts from the device to select followers for the journey along with check boxes for user to select any particular contact from the list. A lot of research went behind it on our part to figure out how to implement it.

## Notifications Issue

The biggest challenge we faced was Notification handling. First of all, we needed to identify particular devices of the followers selected by users and trigger notifications accordingly. This was done using the phone numbers as unique identifiers. Afterwards, we also had to handle the mapping of a particular user with its followers and the user’s journey. When this was all done, we needed to ensure that the notification gets triggered when user starts a journey. If he wants to pause or cancel a journey, then we must get the message from him in order to trigger the respective notifications. In case of Panic, we just trigger the notification directly. In all of these stages of the journey, we maintained respective flags such as “ongoing”, “paused”, “aborted”, “ended”. The status “ended” is applied to a journey when the user clicks the “Reached Destination” button and we consider the journey to be finished and a trigger a notification of the same.

# Challenges and Benefits experienced as a Team

# ![Deadline image]({{site.baseurl}}/images/teamwork.png "Challenges and Benefits to work as a Team")

## In our team, all team members belong to different technical backgrounds. It is good to be diverse, but this diversity had its pros and cons. 

* Initially, we had some issues on how to distribute the work in an adequate manner. Through our weekly meetings and the use of Wunderlist to segregate the task distribution process, we were able to overcome this issue.

* Since, some of the team members didn’t have programming experience, especially in Java. It took us some time to support each other, but gradually things started to fall into the grooves. 

## Benefits we gained:

* The project taught us to manage our time efficiently, when working as a team. Along with the development of this application, every blog, presentation and report had a specific deadline. Thus, we got an idea of how projects work with deadlines. In order to streamline the process, we had milestones after a certain stage of development.

* We learned how to deal with conflicts and make some compromises. These are vital skills in our future career when working in a team.

* Given the different cultural backgrounds of team members, we earlier faced some difficulties. However, this was a personal learning experience of each one of the team members. Since, globalisation is increasing day by day, these kind of experiences are necessary to effectively function when working in teams of inter-cultural background.

# About the course

# ![Deadline image]({{site.baseurl}}/images/softwaredevelopment.png "Software Development Cycle")

## The course was a great opportunity to have hands on experience in developing an Android application as well as project management. Therefore, the lecture gave insgight into:

* Agile Software development and the knowledge of SCRUM and its Characteristics.

* Programming skills on Java and Android along with how to use Android Studio.

* The whole process of Software Project development: Analysis, Design, Implementation, Integration and Testing. 

* How to deal with Customers and their requirements.

## Thanks for Dr.-Ing. Sandro Schulze for his lectures and guidance during the entire process of the application development.

## [Go to our next blog for downloading the app - GoHomeSafely !!!](https://dbse-teaching.github.io/isee2019-streichholz/blog/2019/07/03/Play-Store-Entry/)
