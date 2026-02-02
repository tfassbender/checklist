import { Link } from 'react-router-dom'
import { CheckListSummary } from '../types'

interface ListCardProps {
  list: CheckListSummary
  onDelete?: (id: string) => void
  onToggleActive?: (id: string) => void
}

export default function ListCard({ list, onDelete, onToggleActive }: ListCardProps) {
  const isComplete = list.itemCount > 0 && list.checkedCount === list.itemCount
  const progressPercent =
    list.itemCount > 0 ? (list.checkedCount / list.itemCount) * 100 : 0

  return (
    <div className="card p-4 group min-w-0">
      <Link to={`/list/${list.id}`} className="block">
        <div className="flex items-start justify-between mb-2 min-w-0">
          <div className="flex items-start gap-2 min-w-0 flex-1">
            {list.active && isComplete && (
              <span className="flex-shrink-0 text-green-500 mt-0.5">
                <svg
                  className="w-5 h-5"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                  <polyline points="22 4 12 14.01 9 11.01" />
                </svg>
              </span>
            )}
            <h3 className="font-medium text-lg break-words pr-2 flex-1 min-w-0">{list.name}</h3>
            {!list.active && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-gray-200 dark:bg-gray-700 text-gray-600 dark:text-gray-400 flex-shrink-0 whitespace-nowrap">
                Inactive
              </span>
            )}
          </div>
        </div>
      </Link>

      <div className="flex items-center justify-between mb-3 gap-2">
        <div className="text-sm text-gray-600 dark:text-gray-400">
          {list.active ? (
            list.itemCount === 0
              ? 'No items'
              : `${list.checkedCount}/${list.itemCount} done`
          ) : (
            list.itemCount === 0
              ? 'No items'
              : list.itemCount === 1
              ? '1 item'
              : `${list.itemCount} items`
          )}
        </div>

        <div className="flex gap-1 flex-shrink-0">
          {onToggleActive && (
            <button
              onClick={e => {
                e.preventDefault()
                e.stopPropagation()
                onToggleActive(list.id)
              }}
              className="p-1.5 rounded-lg opacity-0 group-hover:opacity-100
                         hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-600 dark:text-gray-400
                         transition-opacity focus:opacity-100"
              title={list.active ? 'Deactivate list' : 'Activate list'}
            >
              {list.active ? (
                <svg
                  className="w-4 h-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <circle cx="12" cy="12" r="10" />
                  <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
                </svg>
              ) : (
                <svg
                  className="w-4 h-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <polyline points="9 11 12 14 22 4" />
                  <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
                </svg>
              )}
            </button>
          )}
          {onDelete && (
            <button
              onClick={e => {
                e.preventDefault()
                e.stopPropagation()
                onDelete(list.id)
              }}
              className="p-1.5 rounded-lg opacity-0 group-hover:opacity-100
                         hover:bg-red-100 dark:hover:bg-red-900/30 text-red-600 dark:text-red-400
                         transition-opacity focus:opacity-100"
              title="Delete list"
            >
              <svg
                className="w-4 h-4"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
              </svg>
            </button>
          )}
        </div>
      </div>

      <Link to={`/list/${list.id}`} className="block">
        {list.active && list.itemCount > 0 && (
          <div className="h-1.5 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
            <div
              className={`h-full transition-all duration-300 ${
                isComplete ? 'bg-green-500' : 'bg-primary-500'
              }`}
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        )}
      </Link>
    </div>
  )
}
