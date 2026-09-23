/**
 * Official St. Joseph's College (Autonomous), Tiruchirappalli
 * 51-Department Keyword Dictionary & Code Resolver
 */

const DEPARTMENT_MAPPINGS = {
  "AC": "Accounts & Finance",
  "AI": "Department of Artificial Intelligence & Data Science",
  "BI": "Department of Bio-Informatics",
  "BO": "Department of Botany",
  "BT": "Department of Bio-Technology",
  "BU": "Department of Business Administration (BBA)",
  "CB": "Department of Commerce (B.Com CA)",
  "CC": "Department of Commerce (Computer Applications)",
  "CE": "Office of Controller of Examinations",
  "CF": "Department of Commerce (Corporate Secretaryship & Finance)",
  "CH": "Department of Chemistry",
  "CO": "Department of Commerce",
  "COE": "Controller of Examinations Office",
  "CP": "Counselling Psychology & Campus Ministry",
  "CR": "Department of Commerce (Shift II)",
  "CS": "Department of Computer Science",
  "CY": "Department of Cyber Security & IT",
  "DO": "Dean Office & Non-Teaching Staff",
  "DS": "Department of Data Science",
  "EC": "Department of Economics",
  "EH": "Extension & Human Resource",
  "EL": "Department of Electronics",
  "EN": "Department of English",
  "ER": "Executive Office",
  "FC": "Foundation Courses & Ethics",
  "FR": "Department of French",
  "HI": "Department of Hindi",
  "HR": "Department of Human Resource Management (HRM)",
  "HS": "Department of History",
  "IQ": "Internal Quality Assurance Cell (IQAC)",
  "JC": "Jesuit Residence & Management Office",
  "LB": "College Library",
  "LL": "Language Lab",
  "MA": "Department of Mathematics",
  "ML": "Media Lab & Maintenance",
  "PE": "Department of Physical Education",
  "PH": "Department of Physics",
  "PO": "Department of Political Science",
  "S": "Administration Support",
  "S2": "Administration Support II",
  "SA": "Department of Sanskrit",
  "SH": "SHEPHERD Extension Department",
  "SO": "Support Services",
  "SP": "Sports & Physical Education",
  "SS": "Department of Software Systems & Soft Skills",
  "ST": "Department of Statistics",
  "TA": "Department of Tamil",
  "VP": "Vice Principal Office",
  "VT": "Department of Visual Communication (Viscom)",
  "XE": "General Maintenance Staff",
  "XX": "General Support Staff"
};

/**
 * Resolves department code to official full title.
 * Falls back gracefully if an unlisted code is provided.
 */
function getDepartmentFullName(deptCode) {
  if (!deptCode) return 'St. Joseph\'s College';
  const clean = deptCode.trim().toUpperCase();
  return DEPARTMENT_MAPPINGS[clean] || `Department of ${clean}`;
}

module.exports = {
  DEPARTMENT_MAPPINGS,
  getDepartmentFullName
};
