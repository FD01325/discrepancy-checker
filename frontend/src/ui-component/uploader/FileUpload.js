import React, { useState } from 'react';

const FileUpload = () => {
  // State to hold the two files
  const [files, setFiles] = useState({ file1: null, file2: null });

  // Handle file selection
  const handleFileChange = (event, fileKey) => {
    const file = event.target.files[0];
    if (file) {
      setFiles(prevState => ({
        ...prevState,
        [fileKey]: file, // Dynamically set the file based on the key
      }));
    }
  };

  // Handle form submission (example: logging the files or sending them to the server)
  const handleSubmit = (event) => {
    event.preventDefault();

    // Example: Log the files to the console
    console.log('File 1:', files.file1);
    console.log('File 2:', files.file2);

    // You can also send these files to a server here
    // e.g., using FormData to send a POST request
    const formData = new FormData();
    formData.append('file1', files.file1);
    formData.append('file2', files.file2);

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




  return (
    <div>
      <h2>Upload Two Files</h2>
      <form onSubmit={handleSubmit} encType="multipart/form-data">
        <div>
          <label htmlFor="file1">Choose Timesheet 1</label>
          <input
            type="file"
            id="file1"
            onChange={(event) => handleFileChange(event, 'file1')}
          />
        </div>

        <div>
          <label htmlFor="file2">Choose Timesheet 2</label>
          <input
            type="file"
            id="file2"
            onChange={(event) => handleFileChange(event, 'file2')}
          />
        </div>

        <button type="submit">Upload</button>
      </form>

      <div>
        <h3>Selected Files:</h3>
        <p>File 1: {files.file1 ? files.file1.name : 'No file selected'}</p>
        <p>File 2: {files.file2 ? files.file2.name : 'No file selected'}</p>
      </div>
    </div>
  );
};

export default FileUpload;
