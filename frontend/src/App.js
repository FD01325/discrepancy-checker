import React, { useEffect, useState } from "react";
import axios from "axios";
import TwoExcelUploader from "./ui-component/uploader/TwoExcelUploader";
function App() {
  const [message, setMessage] = useState(""); // State to store backend message

  // Fetch data from the Spring Boot backend
  useEffect(() => {
    axios
      .get("/api/hello") // Call the backend API
      .then((response) => {
        setMessage(response.data); // Set the response message
      })
      .catch((error) => {
        console.error("Error fetching data:", error);
      });
  }, []); // Empty dependency array ensures this runs only once after rendering

  return (
    <div style={{ textAlign: "center", padding: "20px" }}>
      <h1>Timesheet Management</h1>
		<TwoExcelUploader/>
    </div>
  );
}

export default App;
