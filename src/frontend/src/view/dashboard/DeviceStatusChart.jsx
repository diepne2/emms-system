export default function DeviceStatusChart() {
  const data = [
    { label: "Running", value: 70, color: "#22c55e" },
    { label: "Maintenance", value: 20, color: "#facc15" },
    { label: "Broken", value: 10, color: "#ef4444" },
  ];

  return (
    <div style={styles.card}>
      <h4>Device Status</h4>

      {data.map((d) => (
        <div key={d.label} style={styles.row}>
          <span>{d.label}</span>
          <div style={styles.barWrap}>
            <div
              style={{
                ...styles.bar,
                width: `${d.value}%`,
                background: d.color,
              }}
            />
          </div>
          <span>{d.value}%</span>
        </div>
      ))}
    </div>
  );
}

const styles = {
  card: {
    background: "#fff",
    padding: 20,
    borderRadius: 10,
  },
  row: {
    display: "flex",
    alignItems: "center",
    gap: 10,
    marginTop: 12,
  },
  barWrap: {
    flex: 1,
    background: "#e5e7eb",
    borderRadius: 6,
    height: 10,
  },
  bar: {
    height: "100%",
    borderRadius: 6,
  },
};
