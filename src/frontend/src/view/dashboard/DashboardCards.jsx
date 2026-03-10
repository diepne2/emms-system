export default function DashboardCards() {
  const cards = [
    { label: "Total Devices", value: 128 },
    { label: "Active Devices", value: 102 },
    { label: "Work Orders", value: 34 },
    { label: "Overdue", value: 5 },
  ];

  return (
    <div style={styles.grid}>
      {cards.map((c) => (
        <div key={c.label} style={styles.card}>
          <div style={styles.value}>{c.value}</div>
          <div style={styles.label}>{c.label}</div>
        </div>
      ))}
    </div>
  );
}

const styles = {
  grid: {
    display: "grid",
    gridTemplateColumns: "repeat(4, 1fr)",
    gap: 16,
  },
  card: {
    background: "#fff",
    padding: 20,
    borderRadius: 10,
    boxShadow: "0 4px 10px rgba(0,0,0,.05)",
    textAlign: "center",
  },
  value: {
    fontSize: 28,
    fontWeight: "bold",
    color: "#2563eb",
  },
  label: {
    marginTop: 6,
    color: "#64748b",
  },
};
