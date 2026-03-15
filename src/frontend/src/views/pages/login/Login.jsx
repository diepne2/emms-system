import React, { useState } from "react"
import { useNavigate } from "react-router-dom"
import logo from "../../../assets/brand/logo.jpg"

export default function Login() {

const navigate = useNavigate()

const [email,setEmail] = useState("")
const [password,setPassword] = useState("")

const handleLogin = () => {

if(email.trim()==="" || password.trim()===""){
alert("Please enter email and password")
return
}

navigate("/dashboard")

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
width:"1100px",
height:"650px",
background:"white",
borderRadius:"14px",
display:"flex",
overflow:"hidden",
boxShadow:"0 30px 60px rgba(0,0,0,0.25)"
},

left:{
flex:1,
padding:"70px",
display:"flex",
flexDirection:"column"
},

header:{
display:"flex",
alignItems:"center",
gap:"10px",
marginBottom:"40px",
fontWeight:"600"
},

dot:{
width:"12px",
height:"12px",
background:"#6c63ff",
borderRadius:"50%"
},

title:{
margin:"0",
fontSize:"36px",
fontWeight:"700"
},

subtitle:{
color:"#777",
marginBottom:"35px"
},

label:{
marginTop:"15px",
fontSize:"14px",
fontWeight:"500"
},

input:{
padding:"14px",
border:"1px solid #e0e0e0",
borderRadius:"8px",
marginTop:"6px",
fontSize:"15px",
outline:"none"
},

options:{
display:"flex",
justifyContent:"space-between",
marginTop:"15px",
fontSize:"14px"
},

loginBtn:{
marginTop:"25px",
padding:"14px",
background:"linear-gradient(90deg,#667eea,#764ba2)",
border:"none",
color:"white",
borderRadius:"8px",
fontSize:"16px",
fontWeight:"600",
cursor:"pointer"
},

googleBtn:{
marginTop:"12px",
padding:"14px",
border:"1px solid #ddd",
background:"#fafafa",
borderRadius:"8px",
cursor:"pointer",
fontSize:"15px"
},

signup:{
marginTop:"25px",
fontSize:"14px",
color:"#555"
},

right:{
flex:1,
background:"linear-gradient(160deg,#f7f8fc,#e8ecf7)",
display:"flex",
alignItems:"center",
justifyContent:"center"
},

logo:{
width:"240px",
filter:"drop-shadow(0 20px 30px rgba(0,0,0,0.2))"
}

}

return(

<div style={styles.container}>

<div style={styles.card}>

{/* LEFT */}

<div style={styles.left}>

<div style={styles.header}>
<div style={styles.dot}></div>
<span>EMMS SYSTEM</span>
</div>

<h1 style={styles.title}>Welcome back</h1>

<p style={styles.subtitle}>
Please enter your login information
</p>

<label style={styles.label}>Email</label>

<input
type="email"
placeholder="Enter your email"
style={styles.input}
value={email}
onChange={(e)=>setEmail(e.target.value)}
/>

<label style={styles.label}>Password</label>

<input
type="password"
placeholder="Enter password"
style={styles.input}
value={password}
onChange={(e)=>setPassword(e.target.value)}
/>

<div style={styles.options}>

<label>
<input type="checkbox"/> Remember for 30 days
</label>

<span
style={{cursor:"pointer",color:"#5a67d8"}}
onClick={()=>navigate("/forgot-password")}
>
Forgot password
</span>

</div>

<button
style={styles.loginBtn}
onClick={handleLogin}
>
Sign in
</button>

<button style={styles.googleBtn}>
Sign in with Google
</button>

</div>

{/* RIGHT */}

<div style={styles.right}>

<img src={logo} style={styles.logo}/>

</div>

</div>

</div>

)

}