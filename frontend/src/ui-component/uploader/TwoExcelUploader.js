import React, { useState } from "react";
import * as XLSX from "xlsx";
import "./style.css"; // Add your custom styles
import axios from "axios";	

const TwoExcelUploader = () => {
  const [file1Data, setFile1Data] = useState([]);
  const [file2Data, setFile2Data] = useState([]);
  const [modalData, setModalData] = useState(null);
  const [showModal, setShowModal] = useState(false);

  const handleFileUpload = (event, setFileData) => {
    const file = event.target.files[0];

    if (file) {
      const reader = new FileReader();
      reader.readAsArrayBuffer(file);

      reader.onload = (e) => {
        const bufferArray = e.target.result;
        const workbook = XLSX.read(bufferArray, { type: "buffer" });

        const sheetName = workbook.SheetNames[0]; // Read the first sheet
        const sheet = workbook.Sheets[sheetName];
        const parsedData = XLSX.utils.sheet_to_json(sheet, { header: 1 }); // Convert sheet to array

        setFileData(parsedData);
      };
    }
  };

  const openPreviewModal = (data) => {
    setModalData(data);
    setShowModal(true);
  };

  // Handle form submission (example: logging the files or sending them to the server)
  const handleSubmit = (event) => {
    event.preventDefault();

    // Example: Log the files to the console
    //console.log('File 1:', files.file1);
   // console.log('File 2:', files.file2);

    // You can also send these files to a server here
    // e.g., using FormData to send a POST request
    const formData = new FormData();
    formData.append('file1', file1Data);
    formData.append('file2', file2Data);


    fetchData(formData)

    // Example fetch POST request (replace with your server URL)
    /*
    fetch('YOUR_SERVER_URL', {
      method: 'POST',
      body: formData,
    })
    .then(response => response.json())
    .then(data => console.log(data))
    .catch(error => console.error('Error:', error));
    */
  };

async function fetchData(formData) {
     const response = await axios.post("http://localhost:8081/api/excel/upload", formData, {
         headers: {
          "Content-Type": "multipart/form-data",
        },
     });
    const data = await response.json();
    console.log(data);
}

  return (
    <div>

      <h2>Upload Timesheet Files (Excel)</h2>

      {/* File 1 Upload */}
 	  <form onSubmit={handleSubmit}>
	      <div>
	        <h3>File 1:</h3>
	        <input
	          type="file"
	          accept=".xlsx, .xls"
	          onChange={(event) => handleFileUpload(event, setFile1Data)}
	        />
	        {file1Data.length > 0 && (
	          <p>
	            <a href="#" onClick={() => openPreviewModal(file1Data)}>
	              Preview File 1
	            </a>
	          </p>
	        )}
	      </div>

	      {/* File 2 Upload */}
	      <div style={{ marginTop: "20px" }}>
	        <h3>File 2:</h3>
	        <input
	          type="file"
	          accept=".xlsx, .xls"
	          onChange={(event) => handleFileUpload(event, setFile2Data)}
	        />
	        {file2Data.length > 0 && (
	          <p>
	            <a href="#" onClick={() => openPreviewModal(file2Data)}>
	              Preview File 2
	            </a>
	          </p>
	        )}
	      </div>

		<button type="submit">Upload</button>
      </form>
      {/* Modal for Preview */}
      {showModal && modalData && (
        <div className="modal-overlay">
          <div className="modal">
            <h3>Excel File Preview</h3>
            <table border="1" style={{ marginTop: "10px", borderCollapse: "collapse" }}>
              <thead>
                <tr>
                  {modalData[0].map((col, index) => (
                    <th key={index}>{col}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {modalData.slice(1).map((row, rowIndex) => (
                  <tr key={rowIndex}>
                    {row.map((cell, colIndex) => (
                      <td key={colIndex}>{cell}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
            <button className="close-btn" onClick={() => setShowModal(false)}>
              Close
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default TwoExcelUploader;
