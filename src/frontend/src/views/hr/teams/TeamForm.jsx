import { useNavigate } from "react-router-dom";
import { useState } from "react";

export default function TeamForm() {
  const navigate = useNavigate();
  const [name, setName] = useState("");

  return (
    <div style={styles.container}>
      <h2>➕ Thêm phòng ban</h2>

      <div style={styles.form}>
        <input
          placeholder="Tên phòng ban"
          value={name}
          onChange={e => setName(e.target.value)}
        />

        <div style={styles.actions}>
          <button onClick={() => navigate(-1)}>Hủy</button>
          <button style={styles.save}>Lưu</button>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { padding: 24, background: "#f9fafb" },
  form: {
    background: "#fff",
    padding: 20,
    borderRadius: 8,
    maxWidth: 400,
  },
  actions: {
    display: "flex",
    justifyContent: "flex-end",
    gap: 8,
    marginTop: 12,
  },
  save: {
    background: "#2563eb",
    color: "#fff",
    border: "none",
    padding: "8px 14px",
    borderRadius: 6,
  },
};
