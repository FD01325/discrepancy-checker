import React, { useState } from "react";
import FileUpload from "../components/FileUpload";
import DownloadButton from "../components/DownloadButton";
import Notification from "../components/Notification";
import axios from "axios";

const Home = () => {
  const [prismFile, setPrismFile] = useState(null);
  const [beelineFile, setBeelineFile] = useState(null);
  const [reportAvailable, setReportAvailable] = useState(false);
  const [notification, setNotification] = useState("");

  const handleUpload = async () => {
    if (!prismFile || !beelineFile) {
      setNotification("Please upload both files.");
      return;
    }

    const formData = new FormData();
    formData.append("prismFile", prismFile);
    formData.append("beelineFile", beelineFile);

    try {
      const response = await axios.post(
        "http://localhost:8080/api/timesheet/compare",
        formData,
        {
          responseType: "blob",
        }
      );

      const blob = new Blob([response.data], {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      });

      setReportAvailable(blob);
      setNotification("Report generated successfully!");
    } catch (error) {
      setNotification("Error generating report.");
    }
  };

  const handleDownload = () => {
    if (!reportAvailable) return;

    const url = window.URL.createObjectURL(reportAvailable);
    const a = document.createElement("a");
    a.href = url;
    a.download = "discrepancies.xlsx";
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-100 p-4">
      <h1 className="text-3xl font-bold mb-6">Timesheet Discrepancy Checker</h1>

      <div className="bg-white shadow-lg p-6 rounded-lg">
        <FileUpload label="Upload Prism Timesheet" onFileSelect={setPrismFile} />
        <FileUpload label="Upload Beeline Timesheet" onFileSelect={setBeelineFile} />


        {/* Centering the button */}
        <div className="flex justify-center mt-4">
          <button
            onClick={handleUpload}
            className="px-6 py-3 bg-blue-500 text-white rounded-lg shadow-md hover:bg-blue-600 transition"
          >
            Generate Report
          </button>
        </div>

        {reportAvailable && <DownloadButton onDownload={handleDownload} />}
      </div>

      <Notification message={notification} onClose={() => setNotification("")} />
    </div>
  );
};

export default Home;
