export default function WorkOrderStatusChart() {
  const data = [
    { label: "Open", value: 12 },
    { label: "In Progress", value: 15 },
    { label: "Completed", value: 7 },
  ];

  return (
    <div style={styles.card}>
      <h4>Work Orders</h4>

      <ul style={styles.list}>
        {data.map((d) => (
          <li key={d.label} style={styles.item}>
            <span>{d.label}</span>
            <strong>{d.value}</strong>
          </li>
        ))}
      </ul>
    </div>
  );
}

const styles = {
  card: {
    background: "#fff",
    padding: 20,
    borderRadius: 10,
  },
  list: {
    marginTop: 12,
    padding: 0,
    listStyle: "none",
  },
  item: {
    display: "flex",
    justifyContent: "space-between",
    padding: "8px 0",
    borderBottom: "1px solid #e5e7eb",
  },
};
