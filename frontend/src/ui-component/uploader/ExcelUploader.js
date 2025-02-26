import React, { useState } from "react";
import * as XLSX from "xlsx";

const ExcelUploader = () => {
  const [data, setData] = useState([]);

  const handleFileUpload = (event) => {
    const file = event.target.files[0];

    if (file) {
      const reader = new FileReader();
      reader.readAsArrayBuffer(file);

      reader.onload = (e) => {
        const bufferArray = e.target.result;
        const workbook = XLSX.read(bufferArray, { type: "buffer" });

        const sheetName = workbook.SheetNames[0]; // Read first sheet
        const sheet = workbook.Sheets[sheetName];
        const parsedData = XLSX.utils.sheet_to_json(sheet, { header: 1 }); // Read data as array of arrays

        setData(parsedData);
      };
    }
  };

  return (
    <div>
      <h2>Upload and Read Excel File</h2>
      <input type="file" accept=".xlsx, .xls" onChange={handleFileUpload} />

      {data.length > 0 && (
        <table border="1" style={{ marginTop: "20px", borderCollapse: "collapse" }}>
          <thead>
            <tr>
              {data[0].map((col, index) => (
                <th key={index}>{col}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.slice(1).map((row, rowIndex) => (
              <tr key={rowIndex}>
                {row.map((cell, colIndex) => (
                  <td key={colIndex}>{cell}</td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ExcelUploader;
