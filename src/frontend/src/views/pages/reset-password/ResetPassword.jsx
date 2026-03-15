import React, { useState } from "react"
import { useNavigate } from "react-router-dom"

export default function ResetPassword() {

const navigate = useNavigate()

const [password,setPassword] = useState("")
const [confirm,setConfirm] = useState("")
const [showPass,setShowPass] = useState(false)
const [showConfirm,setShowConfirm] = useState(false)

const handleSubmit = (e)=>{
e.preventDefault()

if(!password || !confirm){
alert("Please fill all fields")
return
}

if(password !== confirm){
alert("Passwords do not match")
return
}

alert("Password reset successful")

navigate("/login")
}

const styles={

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
marginBottom:"6px"
},

subtitle:{
color:"#777",
marginBottom:"25px"
},

label:{
fontSize:"14px",
fontWeight:"500"
},

inputWrap:{
position:"relative",
marginTop:"8px"
},

input:{
width:"100%",
padding:"14px",
border:"1px solid #ddd",
borderRadius:"8px",
fontSize:"15px",
outline:"none"
},

eye:{
position:"absolute",
right:"12px",
top:"50%",
transform:"translateY(-50%)",
cursor:"pointer",
fontSize:"14px",
color:"#666"
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
marginTop:"18px",
fontSize:"14px",
color:"#667eea",
cursor:"pointer",
display:"inline-block"
}

}

return(

<div style={styles.container}>

<div style={styles.card}>

<h1 style={styles.title}>Reset Password</h1>

<p style={styles.subtitle}>
Enter your new password
</p>

<form onSubmit={handleSubmit}>

<label style={styles.label}>New Password</label>

<div style={styles.inputWrap}>

<input
type={showPass ? "text":"password"}
placeholder="New password"
style={styles.input}
value={password}
onChange={(e)=>setPassword(e.target.value)}
/>

<span
style={styles.eye}
onClick={()=>setShowPass(!showPass)}
>
{showPass ? "Hide":"Show"}
</span>

</div>


<label style={{...styles.label,marginTop:"16px"}}>
Confirm Password
</label>

<div style={styles.inputWrap}>

<input
type={showConfirm ? "text":"password"}
placeholder="Confirm password"
style={styles.input}
value={confirm}
onChange={(e)=>setConfirm(e.target.value)}
/>

<span
style={styles.eye}
onClick={()=>setShowConfirm(!showConfirm)}
>
{showConfirm ? "Hide":"Show"}
</span>

</div>

<button style={styles.btn}>
Reset Password
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