import React, { useState } from "react"
import { useNavigate } from "react-router-dom"

export default function ForgotPassword() {

const navigate = useNavigate()
const [email,setEmail] = useState("")

const handleSubmit = (e)=>{
e.preventDefault()

if(!email){
alert("Please enter email")
return
}

alert("Reset link sent to "+email)
}

const styles = {

container:{
height:"100vh",
display:"flex",
alignItems:"center",
justifyContent:"center",
background:"linear-gradient(135deg,#7f7fd5,#86a8e7,#91eae4)",
fontFamily:"Segoe UI"
},

card:{
width:"420px",
background:"white",
borderRadius:"14px",
padding:"40px",
boxShadow:"0 25px 50px rgba(0,0,0,0.25)"
},

title:{
fontSize:"30px",
fontWeight:"700",
marginBottom:"5px"
},

subtitle:{
color:"#777",
marginBottom:"25px"
},

label:{
fontSize:"14px",
fontWeight:"500"
},

input:{
width:"100%",
padding:"14px",
marginTop:"8px",
border:"1px solid #ddd",
borderRadius:"8px",
fontSize:"15px",
outline:"none"
},

btn:{
width:"100%",
marginTop:"25px",
padding:"14px",
border:"none",
borderRadius:"8px",
fontSize:"16px",
fontWeight:"600",
color:"white",
background:"linear-gradient(90deg,#667eea,#764ba2)",
cursor:"pointer"
},

back:{
marginTop:"20px",
fontSize:"14px",
color:"#667eea",
cursor:"pointer",
display:"inline-block"
}

}

return(

<div style={styles.container}>

<div style={styles.card}>

<h1 style={styles.title}>Forgot Password</h1>

<p style={styles.subtitle}>
Enter your email to receive reset link
</p>

<form onSubmit={handleSubmit}>

<label style={styles.label}>Email</label>

<input
type="email"
placeholder="Enter your email"
style={styles.input}
value={email}
onChange={(e)=>setEmail(e.target.value)}
/>

<button style={styles.btn}>
Send Reset Link
</button>

</form>

<p
style={styles.back}
onClick={()=>navigate("/login")}
>
← Back to Login
</p>

</div>

</div>

)

}