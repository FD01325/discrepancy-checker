import React, { useState } from "react";

const FileUpload = ({ label, onFileSelect }) => {
  const [fileName, setFileName] = useState("");

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setFileName(file.name);
      onFileSelect(file);
    }
  };

  return (
    <div className="flex flex-col items-center">
      <label className="block text-lg font-medium text-gray-700 mb-2">{label}</label>
      <input
        type="file"
        accept=".xlsx"
        className="hidden"
        id={label}
        onChange={handleFileChange}
      />
      <label
        htmlFor={label}
        className="cursor-pointer px-6 py-3 bg-blue-500 text-white rounded-lg shadow-md hover:bg-blue-600 transition"
      >
        Upload File
      </label>
      {fileName && <p className="mt-2 text-gray-600">{fileName}</p>}
    </div>
  );
};

export default FileUpload;
