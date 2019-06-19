import defaultSettings from '@/settings'

const title = defaultSettings.title || 'quickLearning'

export default function getPageTitle(pageTitle) {
  if (pageTitle) {
    return `${pageTitle} - ${title}`
  }
  return `${title}`
}
