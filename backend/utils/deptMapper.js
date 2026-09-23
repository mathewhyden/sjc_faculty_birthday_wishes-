// Official Department Name mappings for St. Joseph's College (Autonomous), Tiruchirappalli
const DEPARTMENT_MAPPINGS = {
  'AC': 'Accounts & Finance',
  'AI': 'Department of Artificial Intelligence & Data Science',
  'BI': 'Department of Bio-Informatics',
  'BO': 'Department of Botany',
  'BT': 'Department of Bio-Technology',
  'BU': 'Department of Business Administration (BBA)',
  'CB': 'Department of Commerce (B.Com CA)',
  'CC': 'Department of Commerce (Computer Applications)',
  'CE': 'Office of Controller of Examinations',
  'CF': 'Department of Commerce (Corporate Secretaryship & Finance)',
  'CH': 'Department of Chemistry',
  'CO': 'Department of Commerce',
  'COE': 'Controller of Examinations Office',
  'CP': 'Counselling Psychology & Campus Ministry',
  'CR': 'Department of Commerce (Shift II)',
  'CS': 'Department of Computer Science',
  'DS': 'Department of Data Science',
  'EC': 'Department of Economics',
  'EL': 'Department of Electronics',
  'EN': 'Department of English',
  'ER': 'Office of Examination Records',
  'ES': 'Department of Environmental Sciences',
  'FA': 'Finance & Administrative Office',
  'FD': 'Department of Food Science & Nutrition',
  'FR': 'Department of French',
  'GY': 'Physical Education & Sports Gymnasium',
  'HI': 'Department of History',
  'HR': 'Department of Human Resource Management',
  'HS': 'Higher Secondary Section Support',
  'IC': 'Information & Communication Centre',
  'IT': 'Department of Information Technology',
  'LA': 'Language Laboratories',
  'LI': 'Arrupe Central Library & Information Center',
  'MA': 'Department of Mathematics',
  'MC': 'Media & Communication Centre',
  'ME': 'Maintenance & Estate Office',
  'ML': 'Modern Language Section',
  'MS': 'Microbiology Section',
  'ND': 'Non-Departmental & Foundation Courses',
  'OFFICE': 'College General Administrative Office',
  'PH': 'Department of Physics',
  'PO': 'Post Graduate & Research Centre',
  'PR': 'Office of the Principal',
  'PS': 'Physical Science Laboratories',
  'RC': 'Rectorate & Jesuit Residence',
  'RE': 'Religious Studies & Ethics',
  'SC': 'Secretary & Campus Management Office',
  'SH': 'Shepherd Extension Programme',
  'SO': 'Department of Social Work',
  'ST': 'Department of Statistics',
  'TA': 'Department of Tamil',
  'TE': 'Teaching Excellence & IQAC',
  'VI': 'Visual Communication',
  'ZO': 'Department of Zoology'
};

function getDepartmentFullName(code) {
  if (!code) return 'St. Joseph\'s College';
  const cleanCode = code.trim().toUpperCase();
  return DEPARTMENT_MAPPINGS[cleanCode] || `Department of ${cleanCode}`;
}

module.exports = {
  DEPARTMENT_MAPPINGS,
  getDepartmentFullName
};
