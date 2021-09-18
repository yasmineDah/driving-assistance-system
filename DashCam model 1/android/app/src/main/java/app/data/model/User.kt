package app.data.model

 class User(Username: String, Email: String) {

     private var username:String = Username
     private var email:String = Email


     fun getUsername(): String{
         return username
     }

     fun setUsername(Username: String){
         username = Username
     }

     fun getEmail(): String{
         return email
     }

     fun setEmail(Email: String){
         email = Email
     }
}