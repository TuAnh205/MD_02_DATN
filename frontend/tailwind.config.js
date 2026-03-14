/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: '#0A6ED8',
        accent: '#FF7600',
        primary: '#0A6ED8',
        secondary: '#FF7600',
        dark: '#1F2937',
        light: '#F8FAFC',
        surface: '#FFFFFF',
      },
      fontFamily: {
        sans: ['Roboto', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        heading: ['Rubik', 'ui-sans-serif', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 4px 12px rgba(15, 23, 42, 0.08)',
        card: '0 2px 12px rgba(15, 23, 42, 0.06)',
      },
    },
  },
  plugins: [],
}
