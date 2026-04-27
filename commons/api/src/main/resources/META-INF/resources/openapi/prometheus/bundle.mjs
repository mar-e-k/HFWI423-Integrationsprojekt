import { exec } from 'child_process';
import { fileURLToPath } from 'url';
import path from 'path';

// Get the directory name of the current module
const __dirname = path.dirname(fileURLToPath(import.meta.url));

// Define absolute paths for input and output files
const inputFile = path.resolve(__dirname, 'prometheus.yaml');
const outputFile = path.resolve(__dirname, 'dist', 'prometheus.bundled.yaml');

// Construct the swagger-cli command
const command = `npx swagger-cli bundle ${inputFile} --outfile ${outputFile} --type yaml`;

console.log(`Running command: ${command}`);

exec(command, (error, stdout, stderr) => {
  if (error) {
    console.error(`Error executing command: ${error.message}`);
    console.error(`stderr: ${stderr}`);
    return;
  }

  if (stdout) {
    console.log(`stdout: ${stdout}`);
  }

  console.log(`✅ OpenAPI spec bundled successfully to ${outputFile}`);
});
