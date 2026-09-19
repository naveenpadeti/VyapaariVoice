/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fff8eb',
          100: '#feedc7',
          200: '#fdda8a',
          300: '#fbc14d',
          400: '#f9a81b',
          500: '#e58e0a',
          600: '#c56d05',
          700: '#9d4e07',
          800: '#7f3e0c',
          900: '#68330e',
        },
        merchant: {
          50: '#f0fdf4',
          100: '#dcfce7',
          500: '#22c55e',
          600: '#16a34a',
          700: '#15803d',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
