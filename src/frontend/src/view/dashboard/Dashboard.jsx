import DashboardCards from "./DashboardCards";
import DeviceStatusChart from "./DeviceStatusChart";
import WorkOrderStatusChart from "./WorkOrderStatusChart";

export default function Dashboard() {
  return (
    <div style={styles.page}>
      <h2>Dashboard EMMS</h2>

      <DashboardCards />

      <div style={styles.row}>
        <DeviceStatusChart />
        <WorkOrderStatusChart />
      </div>
    </div>
  );
}

const styles = {
  page: {
    padding: 24,
    background: "#f5f7fb",
    minHeight: "100vh",
  },
  row: {
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: 20,
    marginTop: 20,
  },
};
